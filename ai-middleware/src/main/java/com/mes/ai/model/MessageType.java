package com.mes.ai.model;

/**
 * 메시지 유형을 구분합니다.
 * 목적: 메시지의 성격(Telemetry/Event/Command/Ack)을 명확히 분리합니다.
 * 이유: 유형별 처리 규칙과 권한 정책이 다르기 때문입니다.
 */
public enum MessageType {
    TELEMETRY,
    EVENT,
    COMMAND,
    ACK
}
