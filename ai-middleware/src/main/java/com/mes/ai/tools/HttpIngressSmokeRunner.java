package com.mes.ai.tools;

import com.mes.ai.ingress.HttpIngressServer;
import com.mes.ai.pipeline.impl.BasicClassifier;
import com.mes.ai.pipeline.impl.BasicValidator;
import com.mes.ai.pipeline.impl.ContentTypeNormalizer;
import com.mes.ai.pipeline.impl.JsonSchemaValidator;
import com.mes.ai.schema.InMemorySchemaRegistry;
import com.mes.ai.schema.SchemaKey;
import com.mes.ai.schema.SimpleJsonSchemaValidator;
import com.mes.ai.service.PipelineOrchestrator;
import com.mes.ai.service.impl.InMemoryQuarantineService;
import com.mes.ai.service.impl.InMemorySecurityScanService;
import com.mes.ai.service.impl.InMemoryStoreService;
import com.mes.ai.service.impl.InMemoryUnknownIngestService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP Ingress 통합 스모크 테스트 실행기입니다.
 * 목적: HTTP 수신 경로가 파이프라인과 연결되는지 빠르게 확인합니다.
 * 기능: 임시 서버 실행 -> 테스트 요청 -> 결과 요약 출력 순서로 진행합니다.
 * 이유: 초보자도 한 번의 실행으로 HTTP 흐름을 검증할 수 있도록 합니다.
 * 유지보수: 확장/변경 시 이 클래스에서 정책을 조정합니다.
 */
public class HttpIngressSmokeRunner {
    /** 스키마 미등록 정책 시스템 속성 키입니다. */
    private static final String MISSING_POLICY_KEY = "ai.schema.missingPolicy";
    /**
     * 목적: main 동작을 수행합니다.
     * 기능: 필요한 처리를 수행합니다.
     * 이유: 기능 흐름을 한 곳에서 담당하기 위함입니다.
     * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
     */
    public static void main(String[] args) {
        // 목적: 실행 환경에 따라 포트/경로를 변경할 수 있게 합니다.
        // 이유: 이미 사용 중인 포트 충돌을 피하기 위함입니다.
        int port = parseInt("ai.http.port", 8080);
        String path = System.getProperty("ai.http.path", "/ingest");

        // 목적: 테스트용 서비스 구성을 준비합니다.
        // 이유: 파이프라인은 단계별 구현체가 모두 필요합니다.
        InMemoryStoreService storeService = new InMemoryStoreService();
        InMemoryQuarantineService quarantineService = new InMemoryQuarantineService();
        InMemoryUnknownIngestService unknownService = new InMemoryUnknownIngestService();

        // 목적: HTTP 입력이 스키마 검증을 통과할 수 있도록 기본 스키마를 준비합니다.
        // 이유: 스키마 미등록 상태에서는 검증 단계에서 격리될 수 있습니다.
        InMemorySchemaRegistry schemaRegistry = new InMemorySchemaRegistry(buildSchemaStore());

        PipelineOrchestrator orchestrator = new PipelineOrchestrator(
                new ContentTypeNormalizer(),
                new BasicClassifier(),
                new JsonSchemaValidator(new BasicValidator(), schemaRegistry, new SimpleJsonSchemaValidator()),
                storeService,
                quarantineService,
                new InMemorySecurityScanService(),
                unknownService
        );

        HttpIngressServer server = new HttpIngressServer(port, path, orchestrator);
        server.start();

        // 목적: 서버가 완전히 준비될 시간을 짧게 확보합니다.
        // 이유: 즉시 요청 시 연결 실패가 나는 상황을 줄입니다.
        sleepQuietly(200);

        // 목적: 정상 케이스 요청을 보내 성공 흐름을 확인합니다.
        // 이유: Ingress -> Normalizer -> Validator -> Store 흐름을 통합 검증합니다.
        String defaultSchemaVersion = System.getProperty("ai.schema.key.schemaVersion", "1.0");
        runCase("성공 케이스", port, path, buildPayload(true, defaultSchemaVersion),
                storeService, quarantineService, unknownService);

        // 목적: 경고 케이스 요청을 보내 경고 통과 흐름을 확인합니다.
        // 이유: 스키마 미등록 경고가 Unknown 기록으로 남는지 확인하기 위함입니다.
        System.setProperty(MISSING_POLICY_KEY, "warn");
        String warnSchemaVersion = System.getProperty("ai.schema.warn.schemaVersion", "2.0");
        runCase("경고 케이스", port, path, buildPayload(true, warnSchemaVersion),
                storeService, quarantineService, unknownService);
        System.setProperty(MISSING_POLICY_KEY, "fail");

        // 목적: 실패 케이스 요청을 보내 격리 흐름을 확인합니다.
        // 이유: 검증 실패 데이터가 Quarantine으로 분기되는지 확인하기 위함입니다.
        runCase("실패 케이스", port, path, buildPayload(false, defaultSchemaVersion),
                storeService, quarantineService, unknownService);

        server.stop();
    }

    /**
     * 목적: HTTP 테스트 요청을 전송합니다.
     * 기능: 필요한 처리를 수행합니다.
     * 이유: 서버 수신 경로를 실제로 호출해 검증하기 위함입니다.
     * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
     */
    private static HttpResponse<String> sendTestRequest(int port, String path, String payload) {
        String url = "http://localhost:" + port + path;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .header("X-Source-Id", "gateway-001")
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        try {
            HttpClient client = HttpClient.newHttpClient();
            return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("HTTP 테스트 요청 전송에 실패했습니다.", ex);
        }
    }

    /**
     * 목적: 성공/실패 테스트용 payload를 생성합니다.
     * 기능: 유효/무효 데이터의 핵심 차이를 반영합니다.
     * 이유: 동일한 흐름으로 정상/격리 분기를 확인하기 위함입니다.
     * 유지보수: 테스트 항목이 늘어나면 이 메서드를 확장합니다.
     */
    private static String buildPayload(boolean valid, String schemaVersion) {
        String messageType = System.getProperty("ai.schema.key.messageType", "TELEMETRY");
        String deviceTypeId = System.getProperty("ai.schema.key.deviceTypeId", "MES");
        String eventId = valid ? "evt-101" : "";
        String timestamp = valid ? "2026-01-01T00:00:00Z" : "2026-01-01 00:00:00";

        return "{"
                + "\"deviceId\":\"device-001\","
                + "\"deviceTypeId\":\"" + deviceTypeId + "\","
                + "\"messageType\":\"" + messageType + "\","
                + "\"timestamp\":\"" + timestamp + "\","
                + "\"eventId\":\"" + eventId + "\","
                + "\"protocolVersion\":\"1.0\","
                + "\"schemaVersion\":\"" + schemaVersion + "\""
                + "}";
    }

    /**
     * 목적: 테스트 결과를 케이스별로 요약 출력합니다.
     * 기능: 응답/저장/격리 카운트를 한 번에 보여줍니다.
     * 이유: 초보자도 성공/실패를 빠르게 확인할 수 있게 하기 위함입니다.
     * 유지보수: 출력 항목 변경 시 이 메서드를 수정합니다.
     */
    private static void printSummary(
            String title,
            HttpResponse<String> response,
            int rawDelta,
            int standardDelta,
            int quarantineDelta,
            int unknownDelta
    ) {
        System.out.println("=== HTTP Ingress 스모크 테스트 결과: " + title + " ===");
        System.out.println("응답 코드: " + response.statusCode());
        System.out.println("응답 본문: " + response.body());
        System.out.println("원본 저장 증가: " + rawDelta);
        System.out.println("표준 저장 증가: " + standardDelta);
        System.out.println("격리 증가: " + quarantineDelta);
        System.out.println("Unknown Ingest 증가: " + unknownDelta);
        System.out.println("==============================================");
    }

    /**
     * 목적: 케이스별 결과를 실행하고 증가 건수를 계산합니다.
     * 기능: 실행 전/후 카운트를 비교해 증가값을 출력합니다.
     * 이유: 누적 카운트로 인한 혼동을 줄이기 위함입니다.
     * 유지보수: 출력 정책 변경 시 이 메서드를 수정합니다.
     */
    private static void runCase(
            String title,
            int port,
            String path,
            String payload,
            InMemoryStoreService storeService,
            InMemoryQuarantineService quarantineService,
            InMemoryUnknownIngestService unknownService
    ) {
        int rawBefore = storeService.getRawStore().size();
        int standardBefore = storeService.getStandardStore().size();
        int quarantineBefore = quarantineService.getRecords().size();
        int unknownBefore = unknownService.getRecords().size();

        HttpResponse<String> response = sendTestRequest(port, path, payload);

        int rawDelta = storeService.getRawStore().size() - rawBefore;
        int standardDelta = storeService.getStandardStore().size() - standardBefore;
        int quarantineDelta = quarantineService.getRecords().size() - quarantineBefore;
        int unknownDelta = unknownService.getRecords().size() - unknownBefore;

        printSummary(title, response, rawDelta, standardDelta, quarantineDelta, unknownDelta);
    }

    /**
     * 목적: 스키마 레지스트리를 구성합니다.
     * 기능: 필요한 처리를 수행합니다.
     * 이유: 테스트 입력이 검증 단계를 통과하도록 기본 스키마를 제공합니다.
     * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
     */
    private static Map<SchemaKey, String> buildSchemaStore() {
        Map<SchemaKey, String> store = new HashMap<>();
        String schemaVersion = System.getProperty("ai.schema.key.schemaVersion", "1.0");
        String messageType = System.getProperty("ai.schema.key.messageType", "TELEMETRY");
        String deviceTypeId = System.getProperty("ai.schema.key.deviceTypeId", "MES");
        SchemaKey key = new SchemaKey(schemaVersion, messageType, deviceTypeId);
        String schemaJson = "{"
                + "\"type\":\"object\","
                + "\"required\":[\"deviceId\",\"deviceTypeId\",\"messageType\",\"timestamp\",\"eventId\",\"protocolVersion\",\"schemaVersion\"],"
                + "\"properties\":{"
                + "\"deviceId\":{\"type\":\"string\"},"
                + "\"deviceTypeId\":{\"type\":\"string\"},"
                + "\"messageType\":{\"type\":\"string\"},"
                + "\"timestamp\":{\"type\":\"string\"},"
                + "\"eventId\":{\"type\":\"string\"},"
                + "\"protocolVersion\":{\"type\":\"string\"},"
                + "\"schemaVersion\":{\"type\":\"string\"}"
                + "}"
                + "}";
        store.put(key, schemaJson);
        return store;
    }

    /**
     * 목적: 숫자 파싱 실패를 안전하게 처리합니다.
     * 기능: 필요한 처리를 수행합니다.
     * 이유: 잘못된 포트 값으로 인한 실행 오류를 방지하기 위함입니다.
     * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
     */
    private static int parseInt(String key, int fallback) {
        String raw = System.getProperty(key);
        if (raw == null || raw.trim().isEmpty()) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    /**
     * 목적: 지연 시간을 안전하게 적용합니다.
     * 기능: 필요한 처리를 수행합니다.
     * 이유: 테스트 시 타이밍 오류를 줄이기 위함입니다.
     * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
     */
    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
