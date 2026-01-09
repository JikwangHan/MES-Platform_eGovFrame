package com.mes.ai.service.impl;

import com.mes.ai.model.ScanQueueItem;
import com.mes.ai.service.AntiVirusScanQueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 메모리 기반 스캔 큐 구현체입니다.
 * 목적: 개발 환경에서 큐 구조를 빠르게 검증합니다.
 * 기능: BlockingQueue로 생산자/소비자 패턴을 제공합니다.
 * 이유: 외부 메시지 브로커 없이도 비동기 흐름을 테스트하기 위함입니다.
 * 유지보수: 운영 환경에서는 브로커 기반 구현체로 교체합니다.
 */
public class InMemoryAntiVirusScanQueue implements AntiVirusScanQueue {
    private final BlockingQueue<ScanQueueItem> queue = new LinkedBlockingQueue<>();

    /**
     * 목적: 스캔 대기 항목을 큐에 적재합니다.
     * 기능: null 체크 후 queue.offer로 추가합니다.
     * 이유: 스캔 워커가 비동기로 처리할 수 있게 하기 위함입니다.
     * 유지보수: 적재 정책 변경 시 이 메서드를 수정합니다.
     */
    @Override
    public void enqueue(ScanQueueItem item) {
        if (item == null) {
            return;
        }
        queue.offer(item);
    }

    /**
     * 목적: 스캔 대기 항목을 큐에서 가져옵니다.
     * 기능: queue.take로 항목을 반환합니다.
     * 이유: 워커가 순차적으로 스캔을 수행하기 때문입니다.
     * 유지보수: 타임아웃/취소 정책 변경 시 이 메서드를 수정합니다.
     */
    @Override
    public ScanQueueItem take() throws InterruptedException {
        return queue.take();
    }
}
