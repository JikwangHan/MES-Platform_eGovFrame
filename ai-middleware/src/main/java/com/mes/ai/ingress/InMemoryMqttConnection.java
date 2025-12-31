package com.mes.ai.ingress;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 메모리 기반 MQTT 연결 구현입니다.
 * 목적: 실제 브로커 없이 수신 흐름을 테스트합니다.
 * 기능: 연결/구독 상태를 유지하고 테스트 메시지를 전달합니다.
 * 이유: 외부 라이브러리 없이도 파이프라인 통합을 검증하기 위함입니다.
 */
public class InMemoryMqttConnection implements MqttConnection {
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private MqttMessageHandler handler;

    /**
     * 목적: 메모리 연결 상태를 활성화합니다.
     * 이유: 테스트 메시지 수신을 가능하게 합니다.
     */
    @Override
    public void connect() {
        // 실제 연결 대신 상태만 활성화합니다.
        connected.set(true);
    }

    /**
     * 목적: 메시지 수신 핸들러를 등록합니다.
     * 이유: 수신된 메시지를 파이프라인으로 전달하기 위함입니다.
     */
    @Override
    public void subscribe(MqttMessageHandler handler) {
        // 테스트 목적이므로 핸들러만 보관합니다.
        this.handler = handler;
    }

    /**
     * 목적: 메모리 연결 상태를 해제합니다.
     * 이유: 테스트 자원을 정리합니다.
     */
    @Override
    public void disconnect() {
        // 연결 상태를 비활성화합니다.
        connected.set(false);
    }

    /**
     * 테스트 메시지를 전달합니다.
     * 목적: 실제 브로커 없이 수신 처리 흐름을 확인합니다.
     */
    public void simulateMessage(String topic, String payload) {
        if (!connected.get()) {
            throw new IllegalStateException("연결되지 않은 상태입니다.");
        }
        if (handler == null) {
            throw new IllegalStateException("수신 핸들러가 설정되지 않았습니다.");
        }
        handler.onMessage(topic, payload);
    }
}
