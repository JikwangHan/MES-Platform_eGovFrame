package com.mes.ai.service.impl;

import com.mes.ai.model.Envelope;
import com.mes.ai.model.RawEnvelope;
import com.mes.ai.service.StoreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mes.ai.util.JacksonUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * JDBC 기반 저장 스켈레톤입니다.
 * 목적: DB 저장 로직의 확장 지점을 제공합니다.
 * 기능: 원본/표준 저장 메서드 시그니처를 고정합니다.
 * 이유: 구현 교체 시에도 호출부를 변경하지 않기 위함입니다.
 * 유지보수: 테이블 구조 변경 시 SQL과 매핑을 이 클래스에서 수정합니다.
 */
public class JdbcStoreService implements StoreService {
    /** 기본 저장 모드 SQL입니다. */
    private static final String INSERT_RAW_SQL_BASIC =
            "INSERT INTO raw_data (received_at, ingress_type, payload, payload_hash, source_id_hash, content_type, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
    /** 기본 저장 모드 SQL입니다. */
    private static final String INSERT_STANDARD_SQL_BASIC =
            "INSERT INTO parsed_data (raw_id, standard_payload, schema_version, protocol_version, created_at) " +
            "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
    /** 확장 저장 모드 SQL입니다. */
    private static final String INSERT_STANDARD_SQL_EXTENDED =
            "INSERT INTO parsed_data (raw_id, standard_payload, schema_version, protocol_version, device_id, message_type, event_id, event_time, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
    /** 저장 모드 시스템 속성 키입니다. */
    private static final String INSERT_MODE_KEY = "ai.jdbc.insertMode";
    /** 확장 저장 모드 값입니다. */
    private static final String INSERT_MODE_EXTENDED = "extended";
    /** 재시도 횟수(기본 1회)입니다. */
    private static final int DEFAULT_RETRY_COUNT = 1;
    /** 재시도 대기 시간(ms)입니다. */
    private static final long DEFAULT_RETRY_DELAY_MS = 300L;

    /** JDBC 연결을 제공하는 DataSource입니다. */
    private final DataSource dataSource;
    /** JSON 직렬화를 위한 ObjectMapper입니다. */
    private final ObjectMapper objectMapper = JacksonUtils.getObjectMapper();

    /**
     * 목적: DataSource를 주입받아 DB 저장을 수행합니다.
     * 기능: DataSource를 내부 필드에 저장합니다.
     * 이유: 연결 설정을 외부로 분리해 유지보수를 쉽게 합니다.
     * 유지보수: 멀티테넌트 분리를 위해 DataSource를 교체할 때 사용합니다.
     */
    public JdbcStoreService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 목적: 원본 데이터를 DB에 저장합니다.
     * 기능: raw_data 테이블에 원본 메타데이터를 기록합니다.
     * 이유: 원본 보존 정책을 만족하기 위함입니다.
     * 유지보수: 컬럼 추가/변경 시 SQL과 매핑을 수정합니다.
     */
    @Override
    public void storeRaw(RawEnvelope rawEnvelope) {
        // 실제 환경에서는 raw_data 테이블에 저장합니다.
        if (rawEnvelope == null) {
            return;
        }
        int maxAttempts = resolveRetryCount();
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(INSERT_RAW_SQL_BASIC, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, rawEnvelope.getReceivedAt());
                statement.setString(2, rawEnvelope.getIngressType());
                statement.setString(3, rawEnvelope.getPayloadBase64());
                statement.setString(4, rawEnvelope.getPayloadHash());
                statement.setString(5, rawEnvelope.getSourceIdHash());
                statement.setString(6, rawEnvelope.getContentType());
                statement.executeUpdate();

                // 생성된 raw_id를 원본에 반영합니다.
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        rawEnvelope.setId(keys.getLong(1));
                    }
                }
                return;
            } catch (SQLException ex) {
                if (attempt == maxAttempts) {
                    throw new IllegalStateException("STORE_RAW_FAILED:원본 데이터 저장에 실패했습니다.", ex);
                }
                sleepRetry(attempt);
            }
        }
    }

    /**
     * 목적: 검증 통과 표준 데이터를 DB에 저장합니다.
     * 기능: parsed_data 테이블에 표준 payload와 버전을 기록합니다.
     * 이유: 후속 시스템이 신뢰 가능한 데이터를 사용하기 위함입니다.
     * 유지보수: 컬럼 추가/변경 시 SQL과 매핑을 수정합니다.
     */
    @Override
    public void storeStandard(Envelope envelope) {
        // 실제 환경에서는 parsed_data 테이블에 저장합니다.
        if (envelope == null) {
            return;
        }
        if (envelope.getRawId() == null) {
            throw new IllegalStateException("raw_id가 없어 표준 데이터를 저장할 수 없습니다.");
        }
        String payloadJson = toJson(envelope.getPayload());
        int maxAttempts = resolveRetryCount();
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(resolveStandardInsertSql())) {
                if (useExtendedMode()) {
                    bindStandardExtended(statement, envelope, payloadJson);
                } else {
                    bindStandardBasic(statement, envelope, payloadJson);
                }
                statement.executeUpdate();
                return;
            } catch (SQLException ex) {
                if (attempt == maxAttempts) {
                    throw new IllegalStateException("STORE_STANDARD_FAILED:표준 데이터 저장에 실패했습니다.", ex);
                }
                sleepRetry(attempt);
            }
        }
    }

    /**
     * 목적: payload를 JSON 문자열로 직렬화합니다.
     * 기능: ObjectMapper를 사용해 JSON 문자열을 생성합니다.
     * 이유: DB에 표준 payload를 일관된 형식으로 저장합니다.
     * 유지보수: 직렬화 옵션 변경 시 ObjectMapper 설정을 조정합니다.
     */
    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            throw new IllegalStateException("payload JSON 직렬화에 실패했습니다.", ex);
        }
    }

    /**
     * 목적: 재시도 횟수를 시스템 속성으로 제어합니다.
     * 기능: 시스템 속성 값을 읽어 재시도 횟수를 계산합니다.
     * 이유: 운영 환경에서 장애 대응 정책을 유연하게 적용합니다.
     * 유지보수: 정책 키 변경 시 이 메서드를 수정합니다.
     */
    private int resolveRetryCount() {
        String raw = System.getProperty("ai.jdbc.retry.count");
        if (raw == null || raw.trim().isEmpty()) {
            return DEFAULT_RETRY_COUNT + 1;
        }
        try {
            int parsed = Integer.parseInt(raw.trim());
            return Math.max(1, parsed + 1);
        } catch (NumberFormatException ex) {
            return DEFAULT_RETRY_COUNT + 1;
        }
    }

    /**
     * 목적: 재시도 간격을 둡니다.
     * 기능: 시도 횟수에 비례한 지연을 부여합니다.
     * 이유: 일시적 DB 오류를 완화하기 위함입니다.
     * 유지보수: 지연 정책 변경 시 상수/로직을 수정합니다.
     */
    private void sleepRetry(int attempt) {
        long delay = DEFAULT_RETRY_DELAY_MS * attempt;
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 목적: 표준 데이터 기본 저장용 SQL을 반환합니다.
     * 기능: 운영 모드에 따라 기본/확장 SQL을 선택합니다.
     * 이유: 운영 환경의 컬럼 구성 차이를 흡수하기 위함입니다.
     * 유지보수: 컬럼 구조 변경 시 상수를 수정합니다.
     */
    private String resolveStandardInsertSql() {
        if (useExtendedMode()) {
            return INSERT_STANDARD_SQL_EXTENDED;
        }
        return INSERT_STANDARD_SQL_BASIC;
    }

    /**
     * 목적: 기본 모드의 표준 데이터 바인딩을 수행합니다.
     * 기능: raw_id/payload/schema/protocol만 저장합니다.
     * 이유: 최소 컬럼 구성을 지원하기 위함입니다.
     * 유지보수: 기본 모드 컬럼이 늘어나면 이 메서드를 수정합니다.
     */
    private void bindStandardBasic(PreparedStatement statement, Envelope envelope, String payloadJson) throws SQLException {
        statement.setLong(1, envelope.getRawId());
        statement.setString(2, payloadJson);
        statement.setString(3, envelope.getSchemaVersion());
        statement.setString(4, envelope.getProtocolVersion());
    }

    /**
     * 목적: 확장 모드의 표준 데이터 바인딩을 수행합니다.
     * 기능: 기본 컬럼 + deviceId/messageType/eventId/timestamp를 저장합니다.
     * 이유: 조회/추적 편의를 높이기 위함입니다.
     * 유지보수: 확장 모드 컬럼이 바뀌면 이 메서드를 수정합니다.
     */
    private void bindStandardExtended(PreparedStatement statement, Envelope envelope, String payloadJson) throws SQLException {
        statement.setLong(1, envelope.getRawId());
        statement.setString(2, payloadJson);
        statement.setString(3, envelope.getSchemaVersion());
        statement.setString(4, envelope.getProtocolVersion());
        statement.setString(5, envelope.getDeviceId());
        statement.setString(6, envelope.getMessageType() == null ? null : envelope.getMessageType().name());
        statement.setString(7, readPayloadValue(envelope, "eventId", "event_id"));
        statement.setString(8, envelope.getTimestamp());
    }

    /**
     * 목적: 확장 모드 저장 여부를 확인합니다.
     * 기능: 시스템 속성 값으로 모드를 판단합니다.
     * 이유: 환경별 컬럼 차이를 코드 수정 없이 대응하기 위함입니다.
     * 유지보수: 속성 키/모드 값 변경 시 상수를 수정합니다.
     */
    private boolean useExtendedMode() {
        String raw = System.getProperty(INSERT_MODE_KEY);
        if (raw == null || raw.trim().isEmpty()) {
            return false;
        }
        return INSERT_MODE_EXTENDED.equalsIgnoreCase(raw.trim());
    }

    /**
     * 목적: payload에서 값을 추출합니다.
     * 기능: 기본 키/별칭 키를 순차적으로 확인합니다.
     * 이유: eventId 등 표준 키가 다양한 이름으로 들어오기 때문입니다.
     * 유지보수: 별칭 추가는 호출부 인자만 확장하면 됩니다.
     */
    private String readPayloadValue(Envelope envelope, String key, String alias) {
        if (envelope == null || envelope.getPayload() == null) {
            return null;
        }
        Object value = envelope.getPayload().get(key);
        if (value == null && alias != null) {
            value = envelope.getPayload().get(alias);
        }
        return value == null ? null : String.valueOf(value).trim();
    }
}
