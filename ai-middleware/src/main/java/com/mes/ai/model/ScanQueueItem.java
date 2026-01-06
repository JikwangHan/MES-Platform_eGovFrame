package com.mes.ai.model;

/**
 * 스캔 큐에 적재하는 항목입니다.
 * 목적: 원본 데이터와 스캔 입력 객체를 함께 묶어 처리합니다.
 * 기능: 큐 워커가 스캔 후 후속 파이프라인으로 전달할 정보를 제공합니다.
 * 이유: 비동기 구조에서 원본 데이터 참조를 잃지 않기 위함입니다.
 */
public class ScanQueueItem {
    /** 원본 수신 데이터입니다. */
    private RawEnvelope rawEnvelope;
    /** 스캔 입력용 인바운드 객체입니다. */
    private InboundObject inboundObject;

    public RawEnvelope getRawEnvelope() {
        return rawEnvelope;
    }

    public void setRawEnvelope(RawEnvelope rawEnvelope) {
        this.rawEnvelope = rawEnvelope;
    }

    public InboundObject getInboundObject() {
        return inboundObject;
    }

    public void setInboundObject(InboundObject inboundObject) {
        this.inboundObject = inboundObject;
    }
}
