package com.mes.ai.model;

/**
 * 스캔 큐에 적재하는 항목입니다.
 * 목적: 원본 데이터와 스캔 입력 객체를 함께 묶어 처리합니다.
 * 기능: 큐 워커가 스캔 후 후속 파이프라인으로 전달할 정보를 제공합니다.
 * 이유: 비동기 구조에서 원본 데이터 참조를 잃지 않기 위함입니다.
 * 유지보수: 확장/변경 시 이 클래스에서 정책을 조정합니다.
 */
public class ScanQueueItem {
    private RawEnvelope rawEnvelope;
    private InboundObject inboundObject;

    /**
     * 목적: 값을 조회합니다.
     * 기능: 현재 설정된 값을 반환합니다.
     * 이유: 외부에서 상태를 확인할 수 있도록 하기 위함입니다.
     */
    public RawEnvelope getRawEnvelope() {
        return rawEnvelope;
    }

    /**
     * 목적: 값을 설정합니다.
     * 기능: 전달받은 값을 내부 필드에 저장합니다.
     * 이유: 모델 상태를 갱신하기 위함입니다.
     */
    public void setRawEnvelope(RawEnvelope rawEnvelope) {
        this.rawEnvelope = rawEnvelope;
    }

    /**
     * 목적: 값을 조회합니다.
     * 기능: 현재 설정된 값을 반환합니다.
     * 이유: 외부에서 상태를 확인할 수 있도록 하기 위함입니다.
     */
    public InboundObject getInboundObject() {
        return inboundObject;
    }

    /**
     * 목적: 값을 설정합니다.
     * 기능: 전달받은 값을 내부 필드에 저장합니다.
     * 이유: 모델 상태를 갱신하기 위함입니다.
     */
    public void setInboundObject(InboundObject inboundObject) {
        this.inboundObject = inboundObject;
    }
}
