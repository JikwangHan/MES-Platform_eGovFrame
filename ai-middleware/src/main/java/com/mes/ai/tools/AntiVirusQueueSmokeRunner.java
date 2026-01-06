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
 */
public class AntiVirusQueueSmokeRunner {
    public static void main(String[] args) throws InterruptedException {
        // 목적: 테스트 환경에서 스캔을 통과하도록 설정합니다.
        // 이유: 기본 스캔 구현체가 ERROR를 반환하면 파이프라인이 진행되지 않습니다.
        System.setProperty("ai.security.scan.mockClean", "true");

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
                new InMemorySecurityScanService(),
                unknownService
        );

        InMemoryAntiVirusScanQueue scanQueue = new InMemoryAntiVirusScanQueue();
        AntiVirusScanSecurityAdapter scanAdapter = new AntiVirusScanSecurityAdapter(new InMemorySecurityScanService());
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
     * 이유: 워커 지연으로 인한 0건 출력 혼선을 줄이기 위함입니다.
     */
    private static void handleOnce(
            InMemoryAntiVirusScanQueue scanQueue,
            AntiVirusScanSecurityAdapter scanAdapter,
            PostScanPipelineHandler postScanHandler
    ) throws InterruptedException {
        com.mes.ai.model.ScanQueueItem item = scanQueue.take();
        postScanHandler.handle(item, scanAdapter.scan(item.getInboundObject()));
    }

    /**
     * 목적: 테스트 입력용 RawEnvelope를 생성합니다.
     * 이유: 큐 기반 스캔 흐름을 간단히 재현하기 위함입니다.
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
}
