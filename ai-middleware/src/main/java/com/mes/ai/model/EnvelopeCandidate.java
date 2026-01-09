package com.mes.ai.model;

import java.util.Map;

/**
 * 정규화 단계에서 생성되는 중간 모델입니다.
 * 목적: 원본 데이터에서 표준 구조로 가기 전, 변환 결과를 임시로 보관합니다.
 * 기능: 원본, 메시지 유형, 정규화된 payload를 함께 담습니다.
 * 이유: 이후 분류/검증 단계에서 동일한 정보를 일관되게 사용하기 위함입니다.
 * 유지보수: 확장/변경 시 이 클래스에서 정책을 조정합니다.
 */
public class EnvelopeCandidate {
    private RawEnvelope rawEnvelope;
    private MessageType messageType;
    private Map<String, Object> normalizedPayload;

    /**
     * 목적: 원본 메시지를 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 검증 실패 시 격리(Quarantine) 처리에 원본이 필요합니다.
     */
    public RawEnvelope getRawEnvelope() {
        return rawEnvelope;
    }

    /**
     * 목적: 원본 메시지를 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 정규화 결과와 원본을 함께 묶어 관리하기 위함입니다.
     */
    public void setRawEnvelope(RawEnvelope rawEnvelope) {
        this.rawEnvelope = rawEnvelope;
    }

    /**
     * 목적: 메시지 유형을 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 처리 흐름(텔레메트리/이벤트/제어 등)을 결정합니다.
     */
    public MessageType getMessageType() {
        return messageType;
    }

    /**
     * 목적: 메시지 유형을 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 분류/검증 단계에서 기준 값으로 사용됩니다.
     */
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    /**
     * 목적: 정규화된 payload를 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 표준 태그 기반의 값 검증 및 저장에 사용됩니다.
     */
    public Map<String, Object> getNormalizedPayload() {
        return normalizedPayload;
    }

    /**
     * 목적: 정규화된 payload를 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 포맷/키 변환 결과를 다음 단계로 전달하기 위함입니다.
     */
    public void setNormalizedPayload(Map<String, Object> normalizedPayload) {
        this.normalizedPayload = normalizedPayload;
    }
}
