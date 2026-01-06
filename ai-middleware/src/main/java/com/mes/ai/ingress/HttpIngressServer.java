package com.mes.ai.ingress;

import com.mes.ai.model.RawEnvelope;
import com.mes.ai.pipeline.impl.HttpIngressHandler;
import com.mes.ai.service.PipelineOrchestrator;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * HTTP 수신 서버입니다.
 * 목적: HTTP POST 요청을 받아 파이프라인으로 전달합니다.
 * 기능: 요청 본문을 읽어 RawEnvelope로 변환 후 오케스트레이터에 전달합니다.
 * 이유: MQTT 미지원 환경에서 HTTP 수신 경로를 제공하기 위함입니다.
 */
public class HttpIngressServer {
    /** 기본 수신 경로입니다. */
    private static final String DEFAULT_PATH = "/ingest";
    /** sourceId를 전달받는 헤더 이름입니다. */
    private static final String SOURCE_ID_HEADER = "X-Source-Id";

    private final HttpServer server;
    private final PipelineOrchestrator orchestrator;
    private final com.mes.ai.service.impl.QueueBasedScanCoordinator scanCoordinator;

    /**
     * 목적: 포트와 오케스트레이터를 주입받아 서버를 초기화합니다.
     * 이유: 실행 환경에 맞는 수신 경로를 쉽게 변경하기 위함입니다.
     */
    public HttpIngressServer(int port, PipelineOrchestrator orchestrator) {
        this(port, DEFAULT_PATH, orchestrator);
    }

    /**
     * 목적: 포트, 경로, 오케스트레이터를 주입받아 서버를 초기화합니다.
     * 이유: 특정 라우팅 경로로 수신을 제한할 수 있게 합니다.
     */
    public HttpIngressServer(int port, String path, PipelineOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
        this.scanCoordinator = null;
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException ex) {
            throw new IllegalStateException("HTTP 서버 생성에 실패했습니다.", ex);
        }
        String normalizedPath = (path == null || path.trim().isEmpty()) ? DEFAULT_PATH : path.trim();
        this.server.createContext(normalizedPath, new IngestHandler());
    }

    /**
     * 목적: 큐 기반 스캔 구조를 사용하는 서버를 초기화합니다.
     * 이유: 스캔을 비동기로 분리해 Ingest 병목을 줄이기 위함입니다.
     */
    public HttpIngressServer(
            int port,
            String path,
            PipelineOrchestrator orchestrator,
            com.mes.ai.service.impl.QueueBasedScanCoordinator scanCoordinator
    ) {
        this.orchestrator = orchestrator;
        this.scanCoordinator = scanCoordinator;
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException ex) {
            throw new IllegalStateException("HTTP 서버 생성에 실패했습니다.", ex);
        }
        String normalizedPath = (path == null || path.trim().isEmpty()) ? DEFAULT_PATH : path.trim();
        this.server.createContext(normalizedPath, new IngestHandler());
    }

    /**
     * 목적: HTTP 수신을 시작합니다.
     * 이유: 외부에서 데이터를 전달할 수 있게 합니다.
     */
    public void start() {
        server.start();
    }

    /**
     * 목적: HTTP 수신을 종료합니다.
     * 이유: 리소스를 안전하게 정리하기 위함입니다.
     */
    public void stop() {
        server.stop(0);
    }

    /**
     * 실제 수신 처리 핸들러입니다.
     * 목적: HTTP 요청을 파이프라인으로 연결합니다.
     */
    private final class IngestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "METHOD_NOT_ALLOWED");
                return;
            }

            Headers headers = exchange.getRequestHeaders();
            String contentType = headers.getFirst("Content-Type");
            String sourceId = headers.getFirst(SOURCE_ID_HEADER);
            if (sourceId == null || sourceId.trim().isEmpty()) {
                // sourceId가 없으면 IP 기반으로 대체합니다.
                sourceId = exchange.getRemoteAddress().getAddress().getHostAddress();
            }

            String payload = readBody(exchange.getRequestBody());
            HttpIngressHandler handler = new HttpIngressHandler(sourceId, contentType);
            RawEnvelope rawEnvelope = handler.receive(payload);
            if (scanCoordinator != null) {
                scanCoordinator.enqueue(rawEnvelope);
            } else if (orchestrator != null) {
                orchestrator.process(rawEnvelope);
            }
            sendResponse(exchange, 202, "ACCEPTED");
        }

        /**
         * 요청 본문을 문자열로 읽습니다.
         * 목적: HTTP payload를 손실 없이 전달하기 위함입니다.
         */
        private String readBody(InputStream inputStream) throws IOException {
            byte[] bytes = inputStream.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        }

        /**
         * 응답을 전송합니다.
         * 목적: 수신 성공/실패를 호출자에게 명확히 전달합니다.
         */
        private void sendResponse(HttpExchange exchange, int status, String message) throws IOException {
            byte[] response = message.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(status, response.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(response);
            }
        }
    }
}
