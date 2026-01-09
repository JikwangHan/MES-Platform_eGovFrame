package com.mes.ai.viewer;

import com.mes.ai.ingress.HttpIngressServer;
import com.mes.ai.pipeline.impl.BasicClassifier;
import com.mes.ai.pipeline.impl.BasicValidator;
import com.mes.ai.pipeline.impl.ContentTypeNormalizer;
import com.mes.ai.pipeline.impl.JsonSchemaValidator;
import com.mes.ai.schema.InMemorySchemaRegistry;
import com.mes.ai.schema.SchemaKey;
import com.mes.ai.schema.SimpleJsonSchemaValidator;
import com.mes.ai.service.PipelineOrchestrator;
import com.mes.ai.service.SecurityScanService;
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

import java.util.HashMap;
import java.util.Map;

/**
 * AI Middleware 뷰어 서비스 실행기입니다.
 * 목적: HTTP 수신과 UI 뷰어를 한 번에 실행합니다.
 * 기능: 수신(ingest) + 스캔 큐 + 뷰어 UI/API를 구동합니다.
 * 이유: 개발 환경에서 빠르게 확인할 수 있도록 통합 실행을 제공합니다.
 * 유지보수: 확장/변경 시 이 클래스에서 정책을 조정합니다.
 */
public class ViewerServiceRunner {
    /**
     * 목적: main 동작을 수행합니다.
     * 기능: 필요한 처리를 수행합니다.
     * 이유: 기능 흐름을 한 곳에서 담당하기 위함입니다.
     * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
     */
    public static void main(String[] args) {
        int ingressPort = parseInt(System.getProperty("ai.viewer.ingressPort"), 8081);
        int viewerPort = parseInt(System.getProperty("ai.viewer.uiPort"), 8082);
        String ingressPath = System.getProperty("ai.viewer.ingressPath", "/ingest");

        // 목적: 메모리 저장소를 구성합니다.
        // 이유: 로컬 환경에서 DB 없이도 뷰어 테스트가 가능해야 합니다.
        InMemoryStoreService storeService = new InMemoryStoreService();
        InMemoryQuarantineService quarantineService = new InMemoryQuarantineService();
        InMemoryUnknownIngestService unknownService = new InMemoryUnknownIngestService();

        InMemorySchemaRegistry schemaRegistry = new InMemorySchemaRegistry(buildSchemaStore());

        SecurityScanService securityScanService = createScanService();

        PipelineOrchestrator orchestrator = new PipelineOrchestrator(
                new ContentTypeNormalizer(),
                new BasicClassifier(),
                new JsonSchemaValidator(new BasicValidator(), schemaRegistry, new SimpleJsonSchemaValidator()),
                storeService,
                quarantineService,
                securityScanService,
                unknownService
        );

        // 목적: 큐 기반 스캔 구조를 활성화합니다.
        // 이유: 수신과 스캔을 분리해 병목을 줄이기 위함입니다.
        InMemoryAntiVirusScanQueue scanQueue = new InMemoryAntiVirusScanQueue();
        AntiVirusScanSecurityAdapter scanAdapter = new AntiVirusScanSecurityAdapter(securityScanService);
        PostScanPipelineHandler postScanHandler = new PostScanPipelineHandler(orchestrator, unknownService);
        QueueBasedScanCoordinator coordinator = new QueueBasedScanCoordinator(scanQueue);

        AntiVirusScanWorker worker = new AntiVirusScanWorker(scanQueue, scanAdapter, postScanHandler);
        Thread workerThread = new Thread(worker, "ai-av-scan-worker");
        workerThread.setDaemon(true);
        workerThread.start();

        HttpIngressServer ingressServer = new HttpIngressServer(ingressPort, ingressPath, orchestrator, coordinator);
        ViewerHttpServer viewerServer = new ViewerHttpServer(viewerPort, storeService, quarantineService, unknownService);

        ingressServer.start();
        viewerServer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            worker.stop();
            ingressServer.stop();
            viewerServer.stop();
        }));

        System.out.println("AI Middleware Viewer Service 시작 완료");
        System.out.println("HTTP Ingest: http://localhost:" + ingressPort + ingressPath);
        System.out.println("Viewer UI  : http://localhost:" + viewerPort + "/viewer");
    }

    /**
     * 목적: 테스트용 스키마 레지스트리를 구성합니다.
     * 기능: 필요한 처리를 수행합니다.
     * 이유: JSON 검증이 통과해야 표준 저장으로 확인할 수 있습니다.
     * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
     */
    private static Map<SchemaKey, String> buildSchemaStore() {
        Map<SchemaKey, String> store = new HashMap<>();
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
     * 목적: 보안 스캔 구현체를 선택합니다.
     * 기능: 필요한 처리를 수행합니다.
     * 이유: 로컬 환경에서 clamd/clamscan/모의 스캔을 유연하게 전환합니다.
     * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
     */
    private static SecurityScanService createScanService() {
        String impl = System.getProperty("ai.security.scan.impl", "inmemory").trim().toLowerCase();
        if ("clamd".equals(impl)) {
            return new ClamAvDaemonScanService();
        }
        if ("clamav".equals(impl)) {
            return new ClamAvSecurityScanService();
        }
        // 목적: 기본 모의 스캔을 사용합니다.
        // 이유: ClamAV가 없어도 테스트를 진행하기 위함입니다.
        System.setProperty("ai.security.scan.mockClean", "true");
        return new InMemorySecurityScanService();
    }

    /**
     * 목적: 숫자 문자열을 안전하게 파싱합니다.
     * 기능: 필요한 처리를 수행합니다.
     * 이유: 잘못된 설정 값이 있어도 기본값으로 복구하기 위함입니다.
     * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
     */
    private static int parseInt(String value, int fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
