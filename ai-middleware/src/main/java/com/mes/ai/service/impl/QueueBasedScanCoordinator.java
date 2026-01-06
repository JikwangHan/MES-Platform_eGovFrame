package com.mes.ai.service.impl;

import com.mes.ai.model.InboundObject;
import com.mes.ai.model.RawEnvelope;
import com.mes.ai.model.ScanQueueItem;
import com.mes.ai.service.AntiVirusScanQueue;

/**
 * 큐 기반 스캔 코디네이터입니다.
 * 목적: 수신 데이터 -> 스캔 큐 적재 흐름을 표준화합니다.
 * 기능: RawEnvelope를 InboundObject로 변환해 큐에 적재합니다.
 * 이유: 수신 처리와 스캔 처리를 분리해 병목을 줄이기 위함입니다.
 */
public class QueueBasedScanCoordinator {
    private final AntiVirusScanQueue scanQueue;

    /**
     * 목적: 스캔 큐 구현체를 주입받습니다.
     * 이유: 실제 브로커/메모리 구현을 쉽게 교체하기 위함입니다.
     */
    public QueueBasedScanCoordinator(AntiVirusScanQueue scanQueue) {
        this.scanQueue = scanQueue;
    }

    /**
     * 목적: 원본 데이터를 큐에 적재합니다.
     * 이유: 스캔 워커가 비동기로 처리하도록 분리하기 위함입니다.
     */
    public void enqueue(RawEnvelope rawEnvelope) {
        if (rawEnvelope == null) {
            return;
        }
        InboundObject inbound = toInbound(rawEnvelope);
        ScanQueueItem item = new ScanQueueItem();
        item.setRawEnvelope(rawEnvelope);
        item.setInboundObject(inbound);
        scanQueue.enqueue(item);
    }

    /**
     * 목적: RawEnvelope를 InboundObject로 변환합니다.
     * 이유: 스캔 서비스 입력을 표준화하기 위함입니다.
     */
    private InboundObject toInbound(RawEnvelope rawEnvelope) {
        InboundObject inbound = new InboundObject();
        if (rawEnvelope.getId() != null) {
            inbound.setId(String.valueOf(rawEnvelope.getId()));
        }
        inbound.setSourceId(rawEnvelope.getSourceIdHash());
        inbound.setContentType(rawEnvelope.getContentType());
        inbound.setPayloadHash(rawEnvelope.getPayloadHash());
        inbound.setPayloadBase64(rawEnvelope.getPayloadBase64());
        if (rawEnvelope.getPayloadBase64() != null) {
            inbound.setSizeBytes(rawEnvelope.getPayloadBase64().length());
        }
        return inbound;
    }
}
