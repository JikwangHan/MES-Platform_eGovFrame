package com.mes.ai.ingress;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 메모리 기반 MQTT 연결 구현입니다.
 * 목적: 실제 브로커 없이 수신 흐름을 테스트합니다.
 * 기능: 연결/구독 상태를 유지하고 테스트 메시지를 전달합니다.
 * 이유: 외부 라이브러리 없이도 파이프라인 통합을 검증하기 위함입니다.
 * 유지보수: 테스트 시나리오가 늘어나면 여기서 메시지 규칙을 확장합니다.
 */
public class InMemoryMqttConnection implements MqttConnection {
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private MqttMessageHandler handler;

    /**
     * 목적: 메모리 연결 상태를 활성화합니다.
     * 기능: 연결 상태 플래그를 true로 전환합니다.
     * 이유: 테스트 메시지 수신을 가능하게 하기 위함입니다.
     * 유지보수: 연결 상태 검증 규칙이 바뀌면 이 메서드에 반영합니다.
     */
    @Override
    public void connect() {
        // 실제 연결 대신 상태만 활성화합니다.
        connected.set(true);
    }

    /**
     * 목적: 메시지 수신 핸들러를 등록합니다.
     * 기능: 전달받은 핸들러를 내부에 저장합니다.
     * 이유: 수신된 메시지를 파이프라인으로 전달하기 위함입니다.
     * 유지보수: 다중 핸들러 지원이 필요하면 컬렉션으로 확장합니다.
     */
    @Override
    public void subscribe(MqttMessageHandler handler) {
        // 테스트 목적이므로 핸들러만 보관합니다.
        this.handler = handler;
    }

    /**
     * 목적: 메모리 연결 상태를 해제합니다.
     * 기능: 연결 상태 플래그를 false로 전환합니다.
     * 이유: 테스트 자원을 정리하기 위함입니다.
     * 유지보수: 종료 시 정리 작업이 추가되면 여기서 확장합니다.
     */
    @Override
    public void disconnect() {
        // 연결 상태를 비활성화합니다.
        connected.set(false);
    }

    /**
     * 테스트 메시지를 전달합니다.
     * 목적: 실제 브로커 없이 수신 처리 흐름을 확인합니다.
     * 기능: 등록된 핸들러에 토픽과 payload를 전달합니다.
     * 이유: 파이프라인 수신 경로를 빠르게 검증하기 위함입니다.
     * 유지보수: 메시지 포맷 검증이 필요하면 이 메서드에서 추가합니다.
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
