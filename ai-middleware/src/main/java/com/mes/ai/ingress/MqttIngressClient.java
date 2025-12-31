package com.mes.ai.ingress;

import com.mes.ai.model.RawEnvelope;
import com.mes.ai.pipeline.IngressHandler;
import com.mes.ai.service.PipelineOrchestrator;

/**
 * MQTT 수신과 파이프라인을 연결하는 클라이언트입니다.
 * 목적: 수신 메시지를 RawEnvelope로 변환한 뒤 파이프라인에 전달합니다.
 * 기능: 연결/구독/해제 흐름을 관리합니다.
 * 이유: Ingress와 처리 로직의 결합을 한 곳에서 관리하기 위함입니다.
 */
public class MqttIngressClient {
    private final MqttConnection connection;
    private final IngressHandler ingressHandler;
    private final PipelineOrchestrator orchestrator;

    /**
     * 목적: MQTT 연결과 처리 의존성을 주입받습니다.
     * 이유: 구현 교체와 테스트를 쉽게 하기 위함입니다.
     */
    public MqttIngressClient(
            MqttConnection connection,
            IngressHandler ingressHandler,
            PipelineOrchestrator orchestrator
    ) {
        this.connection = connection;
        this.ingressHandler = ingressHandler;
        this.orchestrator = orchestrator;
    }

    /**
     * 수신을 시작합니다.
     * 목적: 브로커 연결 및 구독을 수행합니다.
     */
    public void start() {
        connection.connect();
        connection.subscribe(new MqttMessageHandler() {
            @Override
            public void onMessage(String topic, String payload) {
                // 수신 payload를 RawEnvelope로 변환 후 파이프라인에 전달합니다.
                RawEnvelope rawEnvelope = ingressHandler.receive(payload);
                orchestrator.process(rawEnvelope);
            }
        });
    }

    /**
     * 수신을 종료합니다.
     * 목적: 연결을 정리하고 자원을 해제합니다.
     */
    public void stop() {
        connection.disconnect();
    }
}
