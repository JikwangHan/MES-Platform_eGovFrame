package com.mes.ai.ingress;

/**
 * MQTT 연결 설정입니다.
 * 목적: 브로커 접속 정보를 한 곳에 모아 관리합니다.
 * 기능: 호스트/포트/인증/토픽/QoS 정보를 보관합니다.
 * 이유: 설정 변경 시 코드 수정 범위를 최소화하기 위함입니다.
 * 유지보수: 환경별 설정 추가 시 이 클래스만 확장하면 됩니다.
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
     * 기능: 입력 파라미터를 내부 필드에 저장합니다.
     * 이유: 접속 시 필요한 최소값을 강제하기 위함입니다.
     * 유지보수: 옵션 값이 늘어나면 생성자 파라미터를 확장합니다.
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
     * 기능: 설정된 호스트 문자열을 반환합니다.
     * 이유: 연결 대상 확인 및 로깅에 필요합니다.
     * 유지보수: 호스트 검증 로직이 필요하면 별도 메서드로 분리합니다.
     */
    public String getHost() {
        return host;
    }

    /**
     * 목적: 브로커 포트를 조회합니다.
     * 기능: 설정된 포트 번호를 반환합니다.
     * 이유: 접속 구성 확인을 위해 필요합니다.
     * 유지보수: 범위 검증이 필요하면 생성자에서 추가합니다.
     */
    public int getPort() {
        return port;
    }

    /**
     * 목적: 클라이언트 ID를 조회합니다.
     * 기능: 설정된 클라이언트 ID를 반환합니다.
     * 이유: 브로커 식별 및 세션 관리에 사용됩니다.
     * 유지보수: ID 생성 규칙이 바뀌면 생성 단계에서 변경합니다.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * 목적: 사용자 이름을 조회합니다.
     * 기능: 설정된 사용자 이름을 반환합니다.
     * 이유: 인증이 필요한 경우 접속 정보로 사용됩니다.
     * 유지보수: 인증 방식 변경 시 관련 필드 확장으로 대응합니다.
     */
    public String getUsername() {
        return username;
    }

    /**
     * 목적: 비밀번호를 조회합니다.
     * 기능: 설정된 비밀번호를 반환합니다.
     * 이유: 인증이 필요한 경우 접속 정보로 사용됩니다.
     * 유지보수: 보안 정책에 따라 저장/암호화 방식을 조정합니다.
     */
    public String getPassword() {
        return password;
    }

    /**
     * 목적: SSL 사용 여부를 조회합니다.
     * 기능: SSL 활성화 플래그를 반환합니다.
     * 이유: 보안 연결 강제 여부를 판단합니다.
     * 유지보수: TLS 버전 옵션 등이 필요하면 확장합니다.
     */
    public boolean isSslEnabled() {
        return sslEnabled;
    }

    /**
     * 목적: 구독 토픽을 조회합니다.
     * 기능: 설정된 토픽 문자열을 반환합니다.
     * 이유: 수신 범위를 명확히 하기 위함입니다.
     * 유지보수: 다중 토픽 지원 시 리스트로 확장합니다.
     */
    public String getTopic() {
        return topic;
    }

    /**
     * 목적: QoS 레벨을 조회합니다.
     * 기능: 설정된 QoS 값을 반환합니다.
     * 이유: 전송 보장 수준을 설정하기 위함입니다.
     * 유지보수: QoS 정책 변경 시 연관 로직을 함께 조정합니다.
     */
    public int getQos() {
        return qos;
    }
}
