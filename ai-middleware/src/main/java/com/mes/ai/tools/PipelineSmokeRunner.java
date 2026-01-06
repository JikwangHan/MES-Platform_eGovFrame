package com.mes.ai.tools;

import com.mes.ai.model.RawEnvelope;
import com.mes.ai.pipeline.impl.BasicClassifier;
import com.mes.ai.pipeline.impl.BasicValidator;
import com.mes.ai.pipeline.impl.ContentTypeNormalizer;
import com.mes.ai.pipeline.impl.JsonSchemaValidator;
import com.mes.ai.schema.InMemorySchemaRegistry;
import com.mes.ai.schema.SchemaKey;
import com.mes.ai.schema.SimpleJsonSchemaValidator;
import com.mes.ai.service.PipelineOrchestrator;
import com.mes.ai.service.impl.InMemoryQuarantineService;
import com.mes.ai.service.impl.ClamAvSecurityScanService;
import com.mes.ai.service.impl.InMemorySecurityScanService;
import com.mes.ai.service.impl.InMemoryStoreService;
import com.mes.ai.service.impl.InMemoryUnknownIngestService;
import com.mes.ai.util.Base64Utils;
import com.mes.ai.util.TimeUtils;

/**
 * 파이프라인 기본 동작을 빠르게 확인하는 스모크 테스트 실행기입니다.
 * 목적: 보안 스캔/Unknown Ingest 분기와 정상 저장 흐름을 간단히 점검합니다.
 * 기능: 정상 JSON 입력과 미정의 입력을 순차 실행해 결과를 출력합니다.
 * 이유: 초보자도 단일 명령으로 흐름을 검증할 수 있게 하기 위함입니다.
 */
public class PipelineSmokeRunner {
    public static void main(String[] args) {
        // 목적: 테스트 실행 전에 기본 서비스 구성을 준비합니다.
        // 이유: 파이프라인은 여러 단계 구현체가 필요합니다.
        InMemoryStoreService storeService = new InMemoryStoreService();
        InMemoryQuarantineService quarantineService = new InMemoryQuarantineService();
        // 목적: 설정에 따라 스캔 구현체를 선택합니다.
        // 이유: 운영 환경에서는 ClamAV를, 테스트 환경에서는 메모리 스캔을 사용합니다.
        Object scanService = createScanService();
        InMemoryUnknownIngestService unknownService = new InMemoryUnknownIngestService();

        // 목적: 스키마 검증을 위한 기본 스키마 레지스트리를 준비합니다.
        // 이유: JSON Schema 연동 흐름을 테스트에 포함시키기 위함입니다.
        InMemorySchemaRegistry schemaRegistry = new InMemorySchemaRegistry(buildSchemaStore());

        PipelineOrchestrator orchestrator = new PipelineOrchestrator(
                new ContentTypeNormalizer(),
                new BasicClassifier(),
                new JsonSchemaValidator(new BasicValidator(), schemaRegistry, new SimpleJsonSchemaValidator()),
                storeService,
                quarantineService,
                (com.mes.ai.service.SecurityScanService) scanService,
                unknownService
        );

        // 목적: 정상 JSON 입력을 구성해 파이프라인 정상 동작을 확인합니다.
        // 이유: 표준 저장 경로가 동작하는지 확인해야 합니다.
        RawEnvelope normal = buildRawEnvelope(
                "mqtt",
                "application/json",
                "{"
                        + "\"deviceId\":\"device-001\","
                        + "\"deviceTypeId\":\"press-01\","
                        + "\"messageType\":\"telemetry\","
                        + "\"timestamp\":\"2026-01-01T00:00:00Z\","
                        + "\"eventId\":\"evt-001\","
                        + "\"protocolVersion\":\"1.0\","
                        + "\"schemaVersion\":\"1.0\""
                        + "}"
        );

        // 목적: CSV 입력을 구성해 CSV 정규화 흐름을 확인합니다.
        // 이유: 최소 1개 비-JSON 포맷 처리가 동작하는지 검증합니다.
        RawEnvelope csvSample = buildRawEnvelope(
                "mqtt",
                "text/csv",
                "deviceId,deviceTypeId,messageType,timestamp,eventId,protocolVersion,schemaVersion\n"
                        + "device-002,press-01,telemetry,2026-01-01T00:00:10Z,evt-002,1.0,1.0"
        );

        // 목적: TSV 입력을 구성해 TSV 정규화 흐름을 확인합니다.
        // 이유: 탭 구분 포맷도 최소 처리 가능한지 검증합니다.
        RawEnvelope tsvSample = buildRawEnvelope(
                "mqtt",
                "text/tab-separated-values",
                "deviceId\tdeviceTypeId\tmessageType\ttimestamp\teventId\tprotocolVersion\tschemaVersion\n"
                        + "device-003\tpress-01\ttelemetry\t2026-01-01T00:00:20Z\tevt-003\t1.0\t1.0"
        );

        // 목적: 미정의 입력을 구성해 Unknown Ingest 분기 동작을 확인합니다.
        // 이유: 비정형/미정의 데이터가 안전하게 격리되는지 확인합니다.
        RawEnvelope unknown = buildRawEnvelope(
                "mqtt",
                "application/octet-stream",
                "raw-binary-payload"
        );

        // 목적: 두 케이스를 순차 처리합니다.
        // 이유: 정상/CSV/TSV/미정의 흐름을 한 번에 확인하기 위함입니다.
        orchestrator.process(normal);
        orchestrator.process(csvSample);
        orchestrator.process(tsvSample);
        orchestrator.process(unknown);

        // 목적: 결과 요약을 출력합니다.
        // 이유: 초보자가 눈으로 확인할 수 있도록 수치를 제공합니다.
        System.out.println("=== 파이프라인 스모크 테스트 결과 ===");
        System.out.println("원본 저장 건수: " + storeService.getRawStore().size());
        System.out.println("표준 저장 건수: " + storeService.getStandardStore().size());
        System.out.println("격리(검증 실패) 건수: " + quarantineService.getRecords().size());
        System.out.println("Unknown Ingest 건수: " + unknownService.getRecords().size());
        // 목적: 첫 번째 격리/Unknown 레코드 요약을 출력합니다.
        // 이유: 테스트 결과를 초보자도 즉시 이해할 수 있게 합니다.
        if (!quarantineService.getRecords().isEmpty()) {
            System.out.println("격리 사유(첫 번째): " + quarantineService.getRecords().get(0).getReason());
        }
        if (!unknownService.getRecords().isEmpty()) {
            System.out.println("Unknown 사유(첫 번째): " + unknownService.getRecords().get(0).getQuarantineReason());
            System.out.println("Unknown contentType(첫 번째): " + unknownService.getRecords().get(0).getContentType());
        }
        System.out.println("==================================");
        System.out.println("보안 스캔이 모두 차단되는 경우, -Dai.security.scan.mockClean=true 옵션을 사용하세요.");
        System.out.println("ClamAV 사용 시: -Dai.security.scan.impl=clamav -Dai.security.scan.command=clamscan");
    }

    /**
     * 목적: RawEnvelope를 생성하는 공통 헬퍼입니다.
     * 이유: 테스트용 입력 생성 로직을 중복 없이 재사용하기 위함입니다.
     */
    private static RawEnvelope buildRawEnvelope(String ingressType, String contentType, String payloadText) {
        RawEnvelope raw = new RawEnvelope();
        raw.setReceivedAt(TimeUtils.nowIsoUtc());
        raw.setIngressType(ingressType);
        raw.setContentType(contentType);
        raw.setPayloadBase64(Base64Utils.encodeString(payloadText));
        return raw;
    }

    /**
     * 목적: 스캔 구현체를 시스템 속성으로 선택합니다.
     * 이유: 환경에 따라 보안 스캔 엔진을 유연하게 교체합니다.
     */
    private static Object createScanService() {
        String impl = System.getProperty("ai.security.scan.impl", "inmemory").trim().toLowerCase();
        if ("clamav".equals(impl)) {
            return new ClamAvSecurityScanService();
        }
        return new InMemorySecurityScanService();
    }

    /**
     * 목적: 테스트용 스키마 레지스트리를 구성합니다.
     * 이유: 스키마 미등록 시 검증 실패하므로 기본 스키마를 제공합니다.
     */
    private static java.util.Map<SchemaKey, String> buildSchemaStore() {
        java.util.Map<SchemaKey, String> store = new java.util.HashMap<>();
        // 목적: 기본 TELEMETRY 스키마를 준비합니다.
        // 이유: 정상 케이스가 스키마 검증을 통과하도록 하기 위함입니다.
        SchemaKey key = new SchemaKey("1.0", "TELEMETRY", "press-01");
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
}
