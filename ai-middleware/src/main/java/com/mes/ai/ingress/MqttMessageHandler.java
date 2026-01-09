package com.mes.ai.ingress;

/**
 * MQTT 메시지 수신 콜백입니다.
 * 목적: 수신된 payload를 파이프라인에 전달합니다.
 * 기능: 토픽과 payload를 전달받아 처리합니다.
 * 이유: 연결 구현과 처리 로직을 분리하기 위함입니다.
 * 유지보수: 필터링/전처리가 필요하면 구현체에서 확장합니다.
 */
public interface MqttMessageHandler {
    /**
     * 메시지 수신 시 호출됩니다.
     * 목적: 수신 데이터를 처리 단계로 전달합니다.
     * 기능: 토픽과 payload를 콜백 파라미터로 전달합니다.
     * 이유: 수신 흐름을 비동기로 전달하기 위함입니다.
     * 유지보수: QoS/메타데이터 추가 시 파라미터 확장을 검토합니다.
     */
    void onMessage(String topic, String payload);
}
