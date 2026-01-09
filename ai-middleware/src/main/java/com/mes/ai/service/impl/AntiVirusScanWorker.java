package com.mes.ai.service.impl;

import com.mes.ai.model.AntiVirusScanResult;
import com.mes.ai.model.ScanQueueItem;
import com.mes.ai.service.AntiVirusScanQueue;
import com.mes.ai.service.AntiVirusScanService;
import com.mes.ai.service.PostScanHandler;

/**
 * 스캔 큐 워커입니다.
 * 목적: 큐에 적재된 항목을 읽어 스캔하고 후속 처리로 전달합니다.
 * 기능: take -> scan -> handle 순서로 처리합니다.
 * 이유: 스캔과 파이프라인 처리를 분리해 병목을 줄이기 위함입니다.
 * 유지보수: 워커 병렬화 정책 변경 시 이 클래스에서 조정합니다.
 */
public class AntiVirusScanWorker implements Runnable {
    private final AntiVirusScanQueue queue;
    private final AntiVirusScanService scanService;
    private final PostScanHandler postScanHandler;
    private volatile boolean running = true;

    /**
     * 목적: 워커가 사용할 큐/스캔 서비스/후처리 핸들러를 주입합니다.
     * 기능: 주입받은 의존성을 내부 필드에 저장합니다.
     * 이유: 테스트/운영 환경에서 구현체를 쉽게 교체하기 위함입니다.
     * 유지보수: 워커 구성 요소가 늘어나면 생성자를 확장합니다.
     */
    public AntiVirusScanWorker(
            AntiVirusScanQueue queue,
            AntiVirusScanService scanService,
            PostScanHandler postScanHandler
    ) {
        this.queue = queue;
        this.scanService = scanService;
        this.postScanHandler = postScanHandler;
    }

    /**
     * 목적: 워커 실행 루프를 제어합니다.
     * 기능: 실행 플래그를 false로 전환해 루프를 종료합니다.
     * 이유: 종료 시 안전하게 중단할 수 있게 합니다.
     * 유지보수: 종료 절차가 복잡해지면 이 메서드를 확장합니다.
     */
    public void stop() {
        running = false;
    }

    /**
     * 목적: 큐에서 항목을 꺼내 스캔 후 후처리로 전달합니다.
     * 기능: 큐 대기→스캔→분기 처리를 반복합니다.
     * 이유: 비동기 스캔 처리로 수신 병목을 완화하기 위함입니다.
     * 유지보수: 예외 처리/백오프 정책 변경 시 이 메서드를 수정합니다.
     */
    @Override
    public void run() {
        while (running) {
            try {
                ScanQueueItem item = queue.take();
                if (item == null || item.getInboundObject() == null) {
                    continue;
                }
                AntiVirusScanResult result = scanService.scan(item.getInboundObject());
                postScanHandler.handle(item, result);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            } catch (RuntimeException ex) {
                // 목적: 워커 중단 없이 예외를 흘려보냅니다.
                // 이유: 단일 예외로 전체 스캔 큐가 멈추는 상황을 막기 위함입니다.
            }
        }
    }
}
