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
import com.mes.ai.service.impl.AntiVirusScanSecurityAdapter;
import com.mes.ai.service.impl.AntiVirusScanWorker;
import com.mes.ai.service.impl.ClamAvDaemonScanService;
import com.mes.ai.service.impl.ClamAvSecurityScanService;
import com.mes.ai.service.impl.InMemoryAntiVirusScanQueue;
import com.mes.ai.service.impl.InMemoryQuarantineService;
import com.mes.ai.service.impl.InMemorySecurityScanService;
import com.mes.ai.service.impl.InMemoryStoreService;
import com.mes.ai.service.impl.InMemoryUnknownIngestService;
import com.mes.ai.service.impl.PostScanPipelineHandler;
import com.mes.ai.service.impl.QueueBasedScanCoordinator;
import com.mes.ai.util.Base64Utils;
import com.mes.ai.util.TimeUtils;

/**
 * 안티바이러스 큐 기반 스모크 테스트 실행기입니다.
 * 목적: 스캔 큐 -> 워커 -> 파이프라인 분기 흐름을 빠르게 확인합니다.
 * 기능: 테스트 입력을 큐에 적재하고 결과 요약을 출력합니다.
 * 이유: 비동기 스캔 구조가 파이프라인과 연결되는지 확인하기 위함입니다.
 * 유지보수: 확장/변경 시 이 클래스에서 정책을 조정합니다.
 */
public class AntiVirusQueueSmokeRunner {
    /**
     * 목적: main 동작을 수행합니다.
     * 기능: 필요한 처리를 수행합니다.
     * 이유: 기능 흐름을 한 곳에서 담당하기 위함입니다.
     * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
     */
    public static void main(String[] args) throws InterruptedException {
        // 목적: 스캔 구현체를 환경에 따라 선택합니다.
        // 이유: clamd/clamscan 실스캔 또는 모의 스캔을 유연하게 테스트하기 위함입니다.
        com.mes.ai.service.SecurityScanService scanService = createScanService();

        InMemoryStoreService storeService = new InMemoryStoreService();
        InMemoryQuarantineService quarantineService = new InMemoryQuarantineService();
        InMemoryUnknownIngestService unknownService = new InMemoryUnknownIngestService();

        InMemorySchemaRegistry schemaRegistry = new InMemorySchemaRegistry(buildSchemaStore());

        PipelineOrchestrator orchestrator = new PipelineOrchestrator(
                new ContentTypeNormalizer(),
                new BasicClassifier(),
                new JsonSchemaValidator(new BasicValidator(), schemaRegistry, new SimpleJsonSchemaValidator()),
                storeService,
                quarantineService,
                scanService,
                unknownService
        );

        InMemoryAntiVirusScanQueue scanQueue = new InMemoryAntiVirusScanQueue();
        AntiVirusScanSecurityAdapter scanAdapter = new AntiVirusScanSecurityAdapter(scanService);
        PostScanPipelineHandler postScanHandler = new PostScanPipelineHandler(orchestrator, unknownService);

        QueueBasedScanCoordinator coordinator = new QueueBasedScanCoordinator(scanQueue);
        RawEnvelope rawEnvelope = buildRawEnvelope(
                "http",
                "application/json",
                "{"
                        + "\"deviceId\":\"device-001\","
                        + "\"deviceTypeId\":\"MES\","
                        + "\"messageType\":\"TELEMETRY\","
                        + "\"timestamp\":\"2026-01-01T00:00:00Z\","
                        + "\"eventId\":\"evt-900\","
                        + "\"protocolVersion\":\"1.0\","
                        + "\"schemaVersion\":\"1.0\""
                        + "}"
        );

        coordinator.enqueue(rawEnvelope);

        // 목적: 스모크 테스트는 큐 기반 흐름을 단일 사이클로 검증합니다.
        // 이유: 비동기 타이밍에 영향을 받지 않도록 결과를 안정적으로 확인합니다.
        handleOnce(scanQueue, scanAdapter, postScanHandler);

        System.out.println("=== 안티바이러스 큐 스모크 결과 ===");
        System.out.println("원본 저장 건수: " + storeService.getRawStore().size());
        System.out.println("표준 저장 건수: " + storeService.getStandardStore().size());
        System.out.println("격리 건수: " + quarantineService.getRecords().size());
        System.out.println("Unknown Ingest 건수: " + unknownService.getRecords().size());
        System.out.println("=================================");

    }

    /**
     * 목적: 비동기 처리 완료를 짧게 대기합니다.
     * 기능: 필요한 처리를 수행합니다.
     * 이유: 워커 지연으로 인한 0건 출력 혼선을 줄이기 위함입니다.
     * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
     */
    private static void handleOnce(
            InMemoryAntiVirusScanQueue scanQueue,
            AntiVirusScanSecurityAdapter scanAdapter,
            PostScanPipelineHandler postScanHandler
    ) throws InterruptedException {
        com.mes.ai.model.ScanQueueItem item = scanQueue.take();
        com.mes.ai.model.AntiVirusScanResult scanResult = scanAdapter.scan(item.getInboundObject());
        if (isDebugScanEnabled()) {
            System.out.println("=== 보안 스캔 진단(큐) ===");
            System.out.println("scanVerdict: " + scanResult.getVerdict());
            System.out.println("scanEngine : " + scanResult.getEngine());
            System.out.println("scanError  : " + scanResult.getErrorMessage());
            System.out.println("scanThreat : " + scanResult.getThreatName());
            System.out.println("=========================");
        }
        postScanHandler.handle(item, scanResult);
    }

    /**
     * 목적: 테스트 입력용 RawEnvelope를 생성합니다.
     * 기능: 필요한 처리를 수행합니다.
     * 이유: 큐 기반 스캔 흐름을 간단히 재현하기 위함입니다.
     * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
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
         * 목적: 테스트용 스키마 레지스트리를 준비합니다.
         * 기능: 필요한 동작을 수행합니다.
         * 이유: 검증 단계가 통과하도록 기본 스키마를 제공합니다.
         */
    private static java.util.Map<SchemaKey, String> buildSchemaStore() {
        java.util.Map<SchemaKey, String> store = new java.util.HashMap<>();
        SchemaKey key = new SchemaKey("1.0", "TELEMETRY", "MES");
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
         * 목적: 스캔 구현체를 시스템 속성으로 선택합니다.
         * 기능: 필요한 동작을 수행합니다.
         * 이유: 큐 스모크 테스트에서 실제 스캔 여부를 제어하기 위함입니다.
         */
    private static com.mes.ai.service.SecurityScanService createScanService() {
        String impl = System.getProperty("ai.security.scan.impl", "inmemory").trim().toLowerCase();
        if ("clamd".equals(impl)) {
            return new ClamAvDaemonScanService();
        }
        if ("clamav".equals(impl)) {
            return new ClamAvSecurityScanService();
        }
        // 목적: 기본 모의 스캔을 사용합니다.
        // 이유: 로컬 환경에서 ClamAV가 없을 때도 테스트가 가능해야 합니다.
        System.setProperty("ai.security.scan.mockClean", "true");
        return new InMemorySecurityScanService();
    }

    /**
     * 목적: 진단 출력 여부를 확인합니다.
     * 기능: 조건/상태 여부를 반환합니다.
     * 이유: 기본 출력은 간결하게 유지하고, 장애 시에만 상세 정보를 확인하기 위함입니다.
     */
    private static boolean isDebugScanEnabled() {
        String flag = System.getProperty("ai.security.scan.debug", "false");
        return "true".equalsIgnoreCase(flag.trim());
    }
}
