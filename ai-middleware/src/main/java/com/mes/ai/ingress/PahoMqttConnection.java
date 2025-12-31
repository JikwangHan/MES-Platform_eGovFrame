package com.mes.ai.ingress;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;

/**
 * Eclipse Paho 기반 MQTT 연결 구현입니다.
 * 목적: 실제 브로커에 연결해 메시지를 수신합니다.
 * 기능: 연결/구독/해제 및 수신 콜백 처리를 제공합니다.
 * 이유: 실서비스 환경에서 신뢰성 있는 MQTT 통신이 필요합니다.
 */
public class PahoMqttConnection implements MqttConnection {
    private final MqttConnectionConfig config;
    private MqttClient client;

    /**
     * 목적: 연결 설정을 주입받습니다.
     * 이유: 환경별 브로커 설정을 외부에서 주입하기 위함입니다.
     */
    public PahoMqttConnection(MqttConnectionConfig config) {
        this.config = config;
    }

    /**
     * 목적: 브로커에 연결합니다.
     * 이유: 메시지 수신을 시작하기 위한 준비 단계입니다.
     */
    @Override
    public void connect() {
        try {
            String protocol = config.isSslEnabled() ? "ssl" : "tcp";
            String brokerUrl = protocol + "://" + config.getHost() + ":" + config.getPort();
            client = new MqttClient(brokerUrl, config.getClientId(), new MemoryPersistence());

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            if (config.getUsername() != null) {
                options.setUserName(config.getUsername());
            }
            if (config.getPassword() != null) {
                options.setPassword(config.getPassword().toCharArray());
            }

            client.connect(options);
        } catch (MqttException ex) {
            throw new IllegalStateException("MQTT 연결에 실패했습니다.", ex);
        }
    }

    /**
     * 목적: 토픽을 구독하고 수신 콜백을 설정합니다.
     * 이유: 수신된 메시지를 처리 로직으로 전달하기 위함입니다.
     */
    @Override
    public void subscribe(final MqttMessageHandler handler) {
        if (client == null || !client.isConnected()) {
            throw new IllegalStateException("MQTT 연결이 준비되지 않았습니다.");
        }
        try {
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    // 자동 재연결 옵션을 사용하므로 별도 처리 없이 둡니다.
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                    handler.onMessage(topic, payload);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // 수신 전용이므로 처리하지 않습니다.
                }
            });
            client.subscribe(config.getTopic(), config.getQos());
        } catch (MqttException ex) {
            throw new IllegalStateException("MQTT 구독에 실패했습니다.", ex);
        }
    }

    /**
     * 목적: 연결을 종료합니다.
     * 이유: 리소스를 정리하고 안전하게 종료하기 위함입니다.
     */
    @Override
    public void disconnect() {
        if (client == null) {
            return;
        }
        try {
            if (client.isConnected()) {
                client.disconnect();
            }
            client.close();
        } catch (MqttException ex) {
            throw new IllegalStateException("MQTT 연결 해제에 실패했습니다.", ex);
        }
    }
}
