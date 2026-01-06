package com.mes.ai.service;

import com.mes.ai.model.ScanQueueItem;

/**
 * 안티바이러스 스캔 큐 인터페이스입니다.
 * 목적: 스캔 대기 항목을 비동기로 처리하기 위한 계약을 정의합니다.
 * 기능: 큐 적재/대기 조회 기능을 제공합니다.
 * 이유: 실제 구현체(메모리/메시지 브로커)를 교체하기 위함입니다.
 */
public interface AntiVirusScanQueue {
    /**
     * 스캔 큐에 항목을 적재합니다.
     * 목적: 수신 처리와 스캔 처리를 분리합니다.
     */
    void enqueue(ScanQueueItem item);

    /**
     * 스캔 큐에서 항목을 가져옵니다.
     * 목적: 워커가 대기 항목을 처리하도록 합니다.
     */
    ScanQueueItem take() throws InterruptedException;
}
