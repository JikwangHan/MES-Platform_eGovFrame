package com.mes.ai.ingress;

/**
 * MQTT 연결 설정입니다.
 * 목적: 브로커 접속 정보를 한 곳에 모아 관리합니다.
 * 기능: 호스트/포트/인증/토픽/QoS 정보를 보관합니다.
 * 이유: 설정 변경 시 코드 수정 범위를 최소화하기 위함입니다.
 */
public class MqttConnectionConfig {
    private final String host;
    private final int port;
    private final String clientId;
    private final String username;
    private final String password;
    private final boolean sslEnabled;
    private final String topic;
    private final int qos;

    /**
     * 목적: 필수 연결 정보를 설정합니다.
     * 이유: 접속 시 필요한 최소값을 강제합니다.
     */
    public MqttConnectionConfig(
            String host,
            int port,
            String clientId,
            String username,
            String password,
            boolean sslEnabled,
            String topic,
            int qos
    ) {
        this.host = host;
        this.port = port;
        this.clientId = clientId;
        this.username = username;
        this.password = password;
        this.sslEnabled = sslEnabled;
        this.topic = topic;
        this.qos = qos;
    }

    /**
     * 목적: 브로커 호스트를 조회합니다.
     * 이유: 연결 대상 확인 및 로깅에 필요합니다.
     */
    public String getHost() {
        return host;
    }

    /**
     * 목적: 브로커 포트를 조회합니다.
     * 이유: 접속 구성 확인을 위해 필요합니다.
     */
    public int getPort() {
        return port;
    }

    /**
     * 목적: 클라이언트 ID를 조회합니다.
     * 이유: 브로커 식별 및 세션 관리에 사용됩니다.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * 목적: 사용자 이름을 조회합니다.
     * 이유: 인증이 필요한 경우 접속 정보로 사용됩니다.
     */
    public String getUsername() {
        return username;
    }

    /**
     * 목적: 비밀번호를 조회합니다.
     * 이유: 인증이 필요한 경우 접속 정보로 사용됩니다.
     */
    public String getPassword() {
        return password;
    }

    /**
     * 목적: SSL 사용 여부를 조회합니다.
     * 이유: 보안 연결 강제 여부를 판단합니다.
     */
    public boolean isSslEnabled() {
        return sslEnabled;
    }

    /**
     * 목적: 구독 토픽을 조회합니다.
     * 이유: 수신 범위를 명확히 하기 위함입니다.
     */
    public String getTopic() {
        return topic;
    }

    /**
     * 목적: QoS 레벨을 조회합니다.
     * 이유: 전송 보장 수준을 설정하기 위함입니다.
     */
    public int getQos() {
        return qos;
    }
}
