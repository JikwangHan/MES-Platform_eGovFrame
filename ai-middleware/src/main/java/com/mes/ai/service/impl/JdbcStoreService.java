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
 */
public class JdbcStoreService implements StoreService {
    private static final String INSERT_RAW_SQL =
            "INSERT INTO raw_data (received_at, ingress_type, payload, payload_hash, source_id_hash, content_type, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
    private static final String INSERT_STANDARD_SQL =
            "INSERT INTO parsed_data (raw_id, standard_payload, schema_version, protocol_version, created_at) " +
            "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
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
     * 이유: 연결 설정을 외부로 분리해 유지보수를 쉽게 합니다.
     */
    public JdbcStoreService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void storeRaw(RawEnvelope rawEnvelope) {
        // 실제 환경에서는 raw_data 테이블에 저장합니다.
        if (rawEnvelope == null) {
            return;
        }
        int maxAttempts = resolveRetryCount();
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(INSERT_RAW_SQL, Statement.RETURN_GENERATED_KEYS)) {
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
                 PreparedStatement statement = connection.prepareStatement(INSERT_STANDARD_SQL)) {
                statement.setLong(1, envelope.getRawId());
                statement.setString(2, payloadJson);
                statement.setString(3, envelope.getSchemaVersion());
                statement.setString(4, envelope.getProtocolVersion());
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
     * 이유: DB에 표준 payload를 일관된 형식으로 저장합니다.
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
     * 이유: 운영 환경에서 장애 대응 정책을 유연하게 적용합니다.
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
     * 이유: 일시적 DB 오류를 완화하기 위함입니다.
     */
    private void sleepRetry(int attempt) {
        long delay = DEFAULT_RETRY_DELAY_MS * attempt;
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
