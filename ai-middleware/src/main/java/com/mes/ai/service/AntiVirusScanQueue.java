package com.mes.ai.service;

import com.mes.ai.model.ScanQueueItem;

/**
 * 안티바이러스 스캔 큐 인터페이스입니다.
 * 목적: 스캔 대기 항목을 비동기로 처리하기 위한 계약을 정의합니다.
 * 기능: 큐 적재/대기 조회 기능을 제공합니다.
 * 이유: 실제 구현체(메모리/메시지 브로커)를 교체하기 위함입니다.
 * 유지보수: 큐 인프라 변경 시 구현체만 교체합니다.
 */
public interface AntiVirusScanQueue {
    /**
     * 스캔 큐에 항목을 적재합니다.
     * 목적: 수신 처리와 스캔 처리를 분리합니다.
     * 기능: 스캔 대상 항목을 큐에 넣습니다.
     * 이유: 수신 경로의 병목을 줄이기 위함입니다.
     * 유지보수: 큐 적재 방식 변경 시 구현체를 수정합니다.
     */
    void enqueue(ScanQueueItem item);

    /**
     * 스캔 큐에서 항목을 가져옵니다.
     * 목적: 워커가 대기 항목을 처리하도록 합니다.
     * 기능: 큐에서 항목을 꺼내 반환합니다.
     * 이유: 워커가 순차적으로 스캔을 수행하기 때문입니다.
     * 유지보수: 블로킹/타임아웃 정책 변경 시 구현체를 조정합니다.
     */
    ScanQueueItem take() throws InterruptedException;
}
