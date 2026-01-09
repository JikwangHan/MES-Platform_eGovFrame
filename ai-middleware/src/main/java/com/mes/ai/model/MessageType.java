package com.mes.ai.model;

/**
 * 메시지 유형을 구분합니다.
 * 목적: 메시지의 성격(Telemetry/Event/Command/Ack)을 명확히 분리합니다.
 * 기능: 역할에 맞는 핵심 동작을 제공합니다.
 * 이유: 유형별 처리 규칙과 권한 정책이 다르기 때문입니다.
 * 유지보수: 확장/변경 시 이 클래스에서 정책을 조정합니다.
 */
public enum MessageType {
    TELEMETRY,
    EVENT,
    COMMAND,
    ACK
}
