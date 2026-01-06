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
 */
public class InMemoryAntiVirusScanQueue implements AntiVirusScanQueue {
    private final BlockingQueue<ScanQueueItem> queue = new LinkedBlockingQueue<>();

    @Override
    public void enqueue(ScanQueueItem item) {
        if (item == null) {
            return;
        }
        queue.offer(item);
    }

    @Override
    public ScanQueueItem take() throws InterruptedException {
        return queue.take();
    }
}
