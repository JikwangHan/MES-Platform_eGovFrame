package com.mes.ai.viewer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mes.ai.service.impl.InMemoryQuarantineService;
import com.mes.ai.service.impl.InMemoryStoreService;
import com.mes.ai.service.impl.InMemoryUnknownIngestService;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI Middleware 뷰어용 HTTP 서버입니다.
 * 목적: 수신/정규화/격리/Unknown 데이터를 웹 브라우저에서 확인합니다.
 * 기능: HTML UI와 JSON API를 제공해 현황을 시각적으로 보여줍니다.
 * 이유: 개발/테스트 단계에서 결과를 빠르게 확인하기 위함입니다.
 * 유지보수: 확장/변경 시 이 클래스에서 정책을 조정합니다.
 */
public class ViewerHttpServer {
    private static final String DEFAULT_VIEW_PATH = "/viewer";
    private static final String DEFAULT_API_PREFIX = "/api";

    private final HttpServer server;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final InMemoryStoreService storeService;
    private final InMemoryQuarantineService quarantineService;
    private final InMemoryUnknownIngestService unknownService;

    /**
     * 목적: 뷰어 서버를 초기화합니다.
     * 기능: 필요한 동작을 수행합니다.
     * 이유: 포트와 메모리 저장소를 주입받아 결과를 제공하기 위함입니다.
     */
    public ViewerHttpServer(
            int port,
            InMemoryStoreService storeService,
            InMemoryQuarantineService quarantineService,
            InMemoryUnknownIngestService unknownService
    ) {
        this.storeService = storeService;
        this.quarantineService = quarantineService;
        this.unknownService = unknownService;
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException ex) {
            throw new IllegalStateException("뷰어 HTTP 서버 생성에 실패했습니다.", ex);
        }
        this.server.createContext("/", new RedirectHandler());
        this.server.createContext(DEFAULT_VIEW_PATH, new HtmlHandler());
        this.server.createContext(DEFAULT_API_PREFIX + "/summary", new SummaryHandler());
        this.server.createContext(DEFAULT_API_PREFIX + "/raw", new RawHandler());
        this.server.createContext(DEFAULT_API_PREFIX + "/standard", new StandardHandler());
        this.server.createContext(DEFAULT_API_PREFIX + "/quarantine", new QuarantineHandler());
        this.server.createContext(DEFAULT_API_PREFIX + "/unknown", new UnknownHandler());
        this.server.createContext(DEFAULT_API_PREFIX + "/decisions", new DecisionHandler());
    }

    /**
     * 목적: 뷰어 서버를 시작합니다.
     * 기능: 필요한 처리를 수행합니다.
     * 이유: UI와 API를 브라우저에서 접근 가능하게 합니다.
     * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
     */
    public void start() {
        server.start();
    }

    /**
     * 목적: 뷰어 서버를 종료합니다.
     * 기능: 필요한 처리를 수행합니다.
     * 이유: 리소스를 안전하게 정리하기 위함입니다.
     * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
     */
    public void stop() {
        server.stop(0);
    }

    /**
     * 목적: 루트 접근 시 UI로 이동시킵니다.
     * 기능: 필요한 동작을 수행합니다.
     * 이유: 사용자 편의를 위해 기본 화면을 제공합니다.
     * 유지보수: 구조 변경 시 이 클래스에서 조정합니다.
     */
    private final class RedirectHandler implements HttpHandler {
        /**
         * 목적: handle 동작을 수행합니다.
         * 기능: 필요한 처리를 수행합니다.
         * 이유: 기능 흐름을 한 곳에서 담당하기 위함입니다.
         * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
         */
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers headers = exchange.getResponseHeaders();
            headers.add("Location", DEFAULT_VIEW_PATH);
            exchange.sendResponseHeaders(302, -1);
        }
    }

    /**
     * 목적: 뷰어 HTML 페이지를 제공합니다.
     * 기능: 필요한 동작을 수행합니다.
     * 이유: 브라우저에서 데이터 흐름을 확인하기 위함입니다.
     * 유지보수: 구조 변경 시 이 클래스에서 조정합니다.
     */
    private final class HtmlHandler implements HttpHandler {
        /**
         * 목적: handle 동작을 수행합니다.
         * 기능: 필요한 처리를 수행합니다.
         * 이유: 기능 흐름을 한 곳에서 담당하기 위함입니다.
         * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
         */
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendText(exchange, 405, "METHOD_NOT_ALLOWED");
                return;
            }
            sendHtml(exchange, buildViewerHtml());
        }
    }

    /**
     * 목적: 요약 API를 제공합니다.
     * 기능: 필요한 동작을 수행합니다.
     * 이유: 원본/표준/격리/Unknown 수량을 빠르게 보여주기 위함입니다.
     * 유지보수: 구조 변경 시 이 클래스에서 조정합니다.
     */
    private final class SummaryHandler implements HttpHandler {
        /**
         * 목적: handle 동작을 수행합니다.
         * 기능: 필요한 처리를 수행합니다.
         * 이유: 기능 흐름을 한 곳에서 담당하기 위함입니다.
         * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
         */
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, Object> summary = new HashMap<>();
            summary.put("rawCount", storeService.getRawStore().size());
            summary.put("standardCount", storeService.getStandardStore().size());
            summary.put("quarantineCount", quarantineService.getRecords().size());
            summary.put("unknownCount", unknownService.getRecords().size());
            sendJson(exchange, summary);
        }
    }

    /**
     * 목적: 원본 데이터 API를 제공합니다.
     * 기능: 필요한 동작을 수행합니다.
     * 이유: 수신 원문을 확인하기 위함입니다.
     * 유지보수: 구조 변경 시 이 클래스에서 조정합니다.
     */
    private final class RawHandler implements HttpHandler {
        /**
         * 목적: handle 동작을 수행합니다.
         * 기능: 필요한 처리를 수행합니다.
         * 이유: 기능 흐름을 한 곳에서 담당하기 위함입니다.
         * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
         */
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sendJson(exchange, List.copyOf(storeService.getRawStore()));
        }
    }

    /**
     * 목적: 표준 데이터 API를 제공합니다.
     * 기능: 필요한 동작을 수행합니다.
     * 이유: 정규화/검증 후 데이터를 확인하기 위함입니다.
     * 유지보수: 구조 변경 시 이 클래스에서 조정합니다.
     */
    private final class StandardHandler implements HttpHandler {
        /**
         * 목적: handle 동작을 수행합니다.
         * 기능: 필요한 처리를 수행합니다.
         * 이유: 기능 흐름을 한 곳에서 담당하기 위함입니다.
         * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
         */
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sendJson(exchange, List.copyOf(storeService.getStandardStore()));
        }
    }

    /**
     * 목적: 격리 데이터 API를 제공합니다.
     * 기능: 필요한 동작을 수행합니다.
     * 이유: 실패/격리된 입력을 확인하기 위함입니다.
     * 유지보수: 구조 변경 시 이 클래스에서 조정합니다.
     */
    private final class QuarantineHandler implements HttpHandler {
        /**
         * 목적: handle 동작을 수행합니다.
         * 기능: 필요한 처리를 수행합니다.
         * 이유: 기능 흐름을 한 곳에서 담당하기 위함입니다.
         * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
         */
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sendJson(exchange, List.copyOf(quarantineService.getRecords()));
        }
    }

    /**
     * 목적: Unknown Ingest API를 제공합니다.
     * 기능: 필요한 동작을 수행합니다.
     * 이유: 미정의/비정형 입력을 확인하기 위함입니다.
     * 유지보수: 구조 변경 시 이 클래스에서 조정합니다.
     */
    private final class UnknownHandler implements HttpHandler {
        /**
         * 목적: handle 동작을 수행합니다.
         * 기능: 필요한 처리를 수행합니다.
         * 이유: 기능 흐름을 한 곳에서 담당하기 위함입니다.
         * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
         */
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sendJson(exchange, List.copyOf(unknownService.getRecords()));
        }
    }

    /**
     * 목적: 파이프라인 결정(표준/격리/Unknown) 요약 API를 제공합니다.
     * 기능: 표준/격리/Unknown 데이터를 하나의 목록으로 합쳐 제공합니다.
     * 이유: 운영 로그 기준의 decision/reasonCode를 UI에서 빠르게 확인하기 위함입니다.
     * 유지보수: 필드 구성이 바뀌면 이 핸들러에서 조정합니다.
     */
    private final class DecisionHandler implements HttpHandler {
        /**
         * 목적: handle 동작을 수행합니다.
         * 기능: 필요한 처리를 수행합니다.
         * 이유: 기능 흐름을 한 곳에서 담당하기 위함입니다.
         * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
         */
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sendJson(exchange, buildDecisionSnapshots());
        }
    }

    /**
     * 목적: JSON 응답을 전송합니다.
     * 기능: 필요한 처리를 수행합니다.
     * 이유: API 응답을 일관되게 제공하기 위함입니다.
     * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
     */
    private void sendJson(HttpExchange exchange, Object payload) throws IOException {
        byte[] bytes = objectMapper.writeValueAsBytes(payload);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    /**
     * 목적: HTML 응답을 전송합니다.
     * 기능: 필요한 처리를 수행합니다.
     * 이유: 뷰어 UI를 브라우저에서 렌더링하기 위함입니다.
     * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
     */
    private void sendHtml(HttpExchange exchange, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    /**
     * 목적: 텍스트 응답을 전송합니다.
     * 기능: 필요한 처리를 수행합니다.
     * 이유: 오류 메시지를 단순하게 전달하기 위함입니다.
     * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
     */
    private void sendText(HttpExchange exchange, int status, String message) throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    /**
     * 목적: 뷰어 HTML을 생성합니다.
     * 기능: 필요한 처리를 수행합니다.
     * 이유: 외부 파일 없이 단일 실행으로 UI를 제공하기 위함입니다.
     * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
     */
    private String buildViewerHtml() {
        return """
                <!doctype html>
                <html lang="ko">
                <head>
                  <meta charset="utf-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1"/>
                  <title>AI Middleware Viewer</title>
                  <style>
                    :root {
                      --bg: #0f131a;
                      --panel: #171c25;
                      --accent: #2cc5a7;
                      --accent-2: #f6b44a;
                      --text: #eef2f7;
                      --muted: #9aa6b2;
                      --danger: #f05d5e;
                    }
                    body {
                      margin: 0;
                      font-family: "Segoe UI", "Pretendard", sans-serif;
                      background: radial-gradient(1200px 600px at 10% 10%, #1a2230, var(--bg));
                      color: var(--text);
                    }
                    header {
                      padding: 24px 32px;
                      border-bottom: 1px solid #263142;
                      display: flex;
                      align-items: center;
                      justify-content: space-between;
                    }
                    header h1 {
                      margin: 0;
                      font-size: 22px;
                      letter-spacing: 0.5px;
                    }
                    header .meta {
                      color: var(--muted);
                      font-size: 12px;
                    }
                    main {
                      display: grid;
                      grid-template-columns: 280px 1fr;
                      gap: 20px;
                      padding: 24px 32px 40px;
                    }
                    .panel {
                      background: var(--panel);
                      border-radius: 14px;
                      padding: 16px 18px;
                      box-shadow: 0 10px 30px rgba(0,0,0,0.2);
                    }
                    .stat {
                      display: flex;
                      justify-content: space-between;
                      padding: 10px 0;
                      border-bottom: 1px dashed #2b3547;
                      font-size: 14px;
                    }
                    .stat:last-child { border-bottom: none; }
                    .stat strong { color: var(--accent-2); }
                    .tab-list {
                      display: grid;
                      gap: 8px;
                      margin-top: 12px;
                    }
                    .tab {
                      background: #202738;
                      padding: 10px 12px;
                      border-radius: 10px;
                      cursor: pointer;
                      border: 1px solid transparent;
                    }
                    .tab.active {
                      border-color: var(--accent);
                      background: #1c2a2d;
                    }
                    .content {
                      min-height: 520px;
                    }
                    .toolbar {
                      display: flex;
                      gap: 8px;
                      margin-bottom: 12px;
                    }
                    button {
                      background: var(--accent);
                      color: #041316;
                      border: none;
                      padding: 8px 12px;
                      border-radius: 8px;
                      font-weight: 600;
                      cursor: pointer;
                    }
                    button.secondary {
                      background: transparent;
                      color: var(--text);
                      border: 1px solid #344258;
                    }
                    select, input {
                      background: #0d121a;
                      color: var(--text);
                      border: 1px solid #2b3547;
                      border-radius: 8px;
                      padding: 6px 10px;
                      font-size: 12px;
                    }
                    pre {
                      background: #0d121a;
                      border-radius: 10px;
                      padding: 14px;
                      font-size: 12px;
                      line-height: 1.6;
                      white-space: pre-wrap;
                      word-break: break-word;
                      color: #d5dde6;
                    }
                    .empty {
                      color: var(--muted);
                      padding: 20px 0;
                    }
                    .danger {
                      color: var(--danger);
                    }
                    @media (max-width: 960px) {
                      main { grid-template-columns: 1fr; }
                    }
                  </style>
                </head>
                <body>
                  <header>
                    <h1>AI Middleware Viewer</h1>
                    <div class="meta">/api 기반 실시간 상태 확인</div>
                  </header>
                  <main>
                    <aside class="panel">
                      <h3>요약</h3>
                      <div id="summary">
                        <div class="stat"><span>원본</span><strong id="rawCount">-</strong></div>
                        <div class="stat"><span>표준</span><strong id="standardCount">-</strong></div>
                        <div class="stat"><span>격리</span><strong id="quarantineCount">-</strong></div>
                        <div class="stat"><span>Unknown</span><strong id="unknownCount">-</strong></div>
                      </div>
                      <div class="tab-list">
                        <div class="tab active" data-tab="raw">Raw</div>
                        <div class="tab" data-tab="standard">Standard</div>
                        <div class="tab" data-tab="quarantine">Quarantine</div>
                        <div class="tab" data-tab="unknown">Unknown</div>
                        <div class="tab" data-tab="decisions">Decisions</div>
                      </div>
                    </aside>
                    <section class="panel content">
                      <div class="toolbar">
                        <button id="refreshBtn">새로고침</button>
                        <button class="secondary" id="copyBtn">JSON 복사</button>
                      </div>
                      <div class="toolbar" id="decisionFilters" style="display:none;">
                        <select id="decisionSelect">
                          <option value="">결정 전체</option>
                          <option value="STANDARD">STANDARD</option>
                          <option value="QUARANTINE">QUARANTINE</option>
                          <option value="UNKNOWN">UNKNOWN</option>
                        </select>
                        <input id="reasonCodeInput" type="text" placeholder="reasonCode 검색" />
                        <input id="textSearchInput" type="text" placeholder="상세 텍스트 검색" />
                        <select id="sortSelect">
                          <option value="decisionAsc">결정 오름차순</option>
                          <option value="decisionDesc">결정 내림차순</option>
                          <option value="reasonAsc">사유코드 오름차순</option>
                          <option value="reasonDesc">사유코드 내림차순</option>
                          <option value="timeDesc">시간 최신순</option>
                          <option value="timeAsc">시간 오래된순</option>
                          <option value="idDesc">ID 내림차순</option>
                          <option value="idAsc">ID 오름차순</option>
                        </select>
                        <select id="pageSizeSelect">
                          <option value="10">10개</option>
                          <option value="20" selected>20개</option>
                          <option value="50">50개</option>
                        </select>
                        <button class="secondary" id="clearFilterBtn">필터 초기화</button>
                      </div>
                      <div class="toolbar" id="decisionPaging" style="display:none;">
                        <button class="secondary" id="prevPageBtn">이전</button>
                        <span id="pageInfo">1 / 1</span>
                        <button class="secondary" id="nextPageBtn">다음</button>
                      </div>
                      <div id="contentArea"></div>
                    </section>
                  </main>
                  <script>
                    const tabs = document.querySelectorAll('.tab');
                    const contentArea = document.getElementById('contentArea');
                    const decisionFilters = document.getElementById('decisionFilters');
                    const decisionPaging = document.getElementById('decisionPaging');
                    const decisionSelect = document.getElementById('decisionSelect');
                    const reasonCodeInput = document.getElementById('reasonCodeInput');
                    const textSearchInput = document.getElementById('textSearchInput');
                    const sortSelect = document.getElementById('sortSelect');
                    const pageSizeSelect = document.getElementById('pageSizeSelect');
                    const clearFilterBtn = document.getElementById('clearFilterBtn');
                    const prevPageBtn = document.getElementById('prevPageBtn');
                    const nextPageBtn = document.getElementById('nextPageBtn');
                    const pageInfo = document.getElementById('pageInfo');
                    const counts = {
                      raw: document.getElementById('rawCount'),
                      standard: document.getElementById('standardCount'),
                      quarantine: document.getElementById('quarantineCount'),
                      unknown: document.getElementById('unknownCount')
                    };
                    let currentTab = 'raw';
                    let latestPayload = null;
                    let latestData = null;
                    let pageIndex = 1;

                    const fetchJson = async (path) => {
                      const res = await fetch(path);
                      if (!res.ok) throw new Error('API 실패: ' + path);
                      return res.json();
                    };

                    const renderContent = (data) => {
                      latestData = data;
                      if (!data || data.length === 0) {
                        contentArea.innerHTML = '<div class="empty">데이터가 없습니다. 먼저 /ingest로 데이터를 전송하세요.</div>';
                        latestPayload = null;
                        return;
                      }
                      const pretty = JSON.stringify(data, null, 2);
                      contentArea.innerHTML = '<pre>' + pretty + '</pre>';
                      latestPayload = pretty;
                    };

                    const applyDecisionFilters = () => {
                      if (currentTab !== 'decisions') {
                        return;
                      }
                      if (!latestData || !Array.isArray(latestData)) {
                        return;
                      }
                      const decisionValue = decisionSelect.value.trim();
                      const reasonValue = reasonCodeInput.value.trim().toLowerCase();
                      const textValue = textSearchInput.value.trim().toLowerCase();
                      let filtered = latestData.filter(item => {
                        if (!item) return false;
                        if (decisionValue && String(item.decision || '').toUpperCase() !== decisionValue) {
                          return false;
                        }
                        if (reasonValue && String(item.reasonCode || '').toLowerCase().indexOf(reasonValue) < 0) {
                          return false;
                        }
                        if (textValue) {
                          const blob = JSON.stringify(item).toLowerCase();
                          if (blob.indexOf(textValue) < 0) {
                            return false;
                          }
                        }
                        return true;
                      });
                      filtered = applyDecisionSort(filtered);
                      const pageSize = parseInt(pageSizeSelect.value, 10);
                      const pageCount = Math.max(1, Math.ceil(filtered.length / pageSize));
                      if (pageIndex > pageCount) {
                        pageIndex = pageCount;
                      }
                      const start = (pageIndex - 1) * pageSize;
                      const end = start + pageSize;
                      const pageItems = filtered.slice(start, end);
                      pageInfo.textContent = pageIndex + " / " + pageCount;
                      prevPageBtn.disabled = pageIndex <= 1;
                      nextPageBtn.disabled = pageIndex >= pageCount;
                      renderContent(pageItems);
                    };

                    const applyDecisionSort = (items) => {
                      const mode = sortSelect.value;
                      const sorted = items.slice();
                      const decisionKey = item => String(item.decision || '');
                      const reasonKey = item => String(item.reasonCode || '');
                      const timeKey = item => {
                        const raw = item.timestamp || item.quarantinedAt || item.receivedAt;
                        if (!raw) return 0;
                        const parsed = Date.parse(raw);
                        return Number.isNaN(parsed) ? 0 : parsed;
                      };
                      const idKey = item => {
                        const raw = item.rawId || item.recordId || 0;
                        const parsed = Number(raw);
                        return Number.isNaN(parsed) ? 0 : parsed;
                      };
                      if (mode === 'decisionDesc') {
                        sorted.sort((a, b) => decisionKey(b).localeCompare(decisionKey(a)));
                      } else if (mode === 'reasonAsc') {
                        sorted.sort((a, b) => reasonKey(a).localeCompare(reasonKey(b)));
                      } else if (mode === 'reasonDesc') {
                        sorted.sort((a, b) => reasonKey(b).localeCompare(reasonKey(a)));
                      } else if (mode === 'timeDesc') {
                        sorted.sort((a, b) => timeKey(b) - timeKey(a));
                      } else if (mode === 'timeAsc') {
                        sorted.sort((a, b) => timeKey(a) - timeKey(b));
                      } else if (mode === 'idDesc') {
                        sorted.sort((a, b) => idKey(b) - idKey(a));
                      } else if (mode === 'idAsc') {
                        sorted.sort((a, b) => idKey(a) - idKey(b));
                      } else {
                        sorted.sort((a, b) => decisionKey(a).localeCompare(decisionKey(b)));
                      }
                      return sorted;
                    };

                    const refreshAll = async () => {
                      try {
                        const summary = await fetchJson('/api/summary');
                        counts.raw.textContent = summary.rawCount;
                        counts.standard.textContent = summary.standardCount;
                        counts.quarantine.textContent = summary.quarantineCount;
                        counts.unknown.textContent = summary.unknownCount;
                        const data = await fetchJson('/api/' + currentTab);
                        renderContent(data);
                        if (currentTab === 'decisions') {
                          applyDecisionFilters();
                        }
                      } catch (err) {
                        contentArea.innerHTML = '<div class="empty danger">API 연결 실패: ' + err.message + '</div>';
                      }
                    };

                    tabs.forEach(tab => {
                      tab.addEventListener('click', async () => {
                        tabs.forEach(t => t.classList.remove('active'));
                        tab.classList.add('active');
                        currentTab = tab.dataset.tab;
                        const showDecisionUi = currentTab === 'decisions';
                        decisionFilters.style.display = showDecisionUi ? 'flex' : 'none';
                        decisionPaging.style.display = showDecisionUi ? 'flex' : 'none';
                        const data = await fetchJson('/api/' + currentTab);
                        renderContent(data);
                        if (currentTab === 'decisions') {
                          pageIndex = 1;
                          applyDecisionFilters();
                        }
                      });
                    });

                    document.getElementById('refreshBtn').addEventListener('click', refreshAll);
                    document.getElementById('copyBtn').addEventListener('click', () => {
                      if (!latestPayload) return;
                      navigator.clipboard.writeText(latestPayload);
                    });
                    [decisionSelect, reasonCodeInput, textSearchInput, sortSelect, pageSizeSelect].forEach(input => {
                      input.addEventListener('input', applyDecisionFilters);
                    });
                    clearFilterBtn.addEventListener('click', () => {
                      decisionSelect.value = '';
                      reasonCodeInput.value = '';
                      textSearchInput.value = '';
                      sortSelect.value = 'decisionAsc';
                      pageSizeSelect.value = '20';
                      pageIndex = 1;
                      applyDecisionFilters();
                    });
                    prevPageBtn.addEventListener('click', () => {
                      if (pageIndex > 1) {
                        pageIndex -= 1;
                        applyDecisionFilters();
                      }
                    });
                    nextPageBtn.addEventListener('click', () => {
                      pageIndex += 1;
                      applyDecisionFilters();
                    });

                    refreshAll();
                    setInterval(refreshAll, 3000);
                  </script>
                </body>
                </html>
                """;
    }

    /**
     * 목적: 표준/격리/Unknown 데이터를 하나의 결정 목록으로 만듭니다.
     * 기능: decision/reasonCode/reasonDetail 필드를 포함해 리스트로 반환합니다.
     * 이유: 운영 로그 표준과 UI를 맞춰 분석을 쉽게 하기 위함입니다.
     * 유지보수: 결정 정책이 바뀌면 이 메서드에서 조정합니다.
     */
    private List<Map<String, Object>> buildDecisionSnapshots() {
        List<Map<String, Object>> items = new ArrayList<>();

        storeService.getStandardStore().forEach(envelope -> {
            Map<String, Object> item = new HashMap<>();
            item.put("decision", "STANDARD");
            item.put("rawId", envelope.getRawId());
            item.put("deviceId", envelope.getDeviceId());
            item.put("messageType", envelope.getMessageType() == null ? null : envelope.getMessageType().name());
            item.put("timestamp", envelope.getTimestamp());
            item.put("reasonCode", "VALIDATION_PASS");
            item.put("reasonDetail", "-");
            items.add(item);
        });

        quarantineService.getRecords().forEach(record -> {
            ReasonParts parts = splitReason(record.getReason());
            Map<String, Object> item = new HashMap<>();
            item.put("decision", "QUARANTINE");
            item.put("rawId", record.getRawEnvelope() == null ? null : record.getRawEnvelope().getId());
            item.put("ingressType", record.getRawEnvelope() == null ? null : record.getRawEnvelope().getIngressType());
            item.put("contentType", record.getRawEnvelope() == null ? null : record.getRawEnvelope().getContentType());
            item.put("reasonCode", parts.code);
            item.put("reasonDetail", safeDetail(parts.detail));
            item.put("quarantinedAt", record.getQuarantinedAt());
            items.add(item);
        });

        unknownService.getRecords().forEach(record -> {
            ReasonParts parts = splitReason(record.getQuarantineReason());
            Map<String, Object> item = new HashMap<>();
            item.put("decision", "UNKNOWN");
            item.put("recordId", record.getId());
            item.put("ingressType", record.getIngressType());
            item.put("contentType", record.getContentType());
            item.put("reasonCode", parts.code);
            item.put("reasonDetail", safeDetail(parts.detail));
            item.put("receivedAt", record.getReceivedAt());
            items.add(item);
        });
        return items;
    }

    /**
     * 목적: 사유 문자열을 코드/상세로 분리합니다.
     * 기능: 콜론 기준으로 reasonCode와 reasonDetail을 나눕니다.
     * 이유: 운영 로그 표준과 동일한 구조를 제공하기 위함입니다.
     * 유지보수: 사유 포맷이 바뀌면 분리 규칙을 수정합니다.
     */
    private ReasonParts splitReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            return new ReasonParts("UNKNOWN", "-");
        }
        int index = reason.indexOf(':');
        if (index < 0) {
            return new ReasonParts(reason.trim(), "-");
        }
        String code = reason.substring(0, index).trim();
        String detail = reason.substring(index + 1).trim();
        return new ReasonParts(code.isEmpty() ? "UNKNOWN" : code, detail.isEmpty() ? "-" : detail);
    }

    /**
     * 목적: 상세 사유의 빈 값을 안전한 문자열로 보정합니다.
     * 기능: null/빈 문자열을 "-"로 치환합니다.
     * 이유: UI에서 빈 값으로 인한 혼란을 줄이기 위함입니다.
     * 유지보수: 표시 정책이 바뀌면 이 메서드를 수정합니다.
     */
    private String safeDetail(String detail) {
        if (detail == null || detail.trim().isEmpty()) {
            return "-";
        }
        return detail;
    }

    /**
     * 목적: 사유 분리 결과를 보관합니다.
     * 기능: 코드/상세를 묶어 전달합니다.
     * 이유: decision API 응답에서 일관된 필드를 제공하기 위함입니다.
     * 유지보수: 사유 구조가 바뀌면 이 클래스도 수정합니다.
     */
    private static final class ReasonParts {
        private final String code;
        private final String detail;

        private ReasonParts(String code, String detail) {
            this.code = code;
            this.detail = detail;
        }
    }
}
