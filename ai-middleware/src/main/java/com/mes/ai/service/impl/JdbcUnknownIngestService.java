package com.mes.ai.service.impl;

import com.mes.ai.model.UnknownIngestRecord;
import com.mes.ai.service.UnknownIngestService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * JDBC 기반 Unknown Ingest 저장 구현체입니다.
 * 목적: 미정의 통신/비정형 데이터를 DB에 안전하게 보관합니다.
 * 기능: unknown_ingest 테이블에 원문과 스캔 정보를 저장합니다.
 * 이유: 운영 파이프라인을 보호하고 분석 근거를 확보하기 위함입니다.
 */
public class JdbcUnknownIngestService implements UnknownIngestService {
    private static final String INSERT_SQL =
            "INSERT INTO unknown_ingest " +
            "(received_at, ingress_type, raw_payload_base64, payload_hash, source_id_hash, content_type, " +
            "scan_status, scan_engine, scan_signature, scan_duration_ms, quarantine_reason, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
    /** 재시도 횟수(기본 1회)입니다. */
    private static final int DEFAULT_RETRY_COUNT = 1;
    /** 재시도 대기 시간(ms)입니다. */
    private static final long DEFAULT_RETRY_DELAY_MS = 300L;

    /** JDBC 연결을 제공하는 DataSource입니다. */
    private final DataSource dataSource;

    /**
     * 목적: DataSource를 주입받아 저장을 수행합니다.
     * 이유: 연결 정보를 외부로 분리해 유지보수를 쉽게 합니다.
     */
    public JdbcUnknownIngestService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public UnknownIngestRecord save(UnknownIngestRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("UnknownIngestRecord가 없습니다.");
        }
        int maxAttempts = resolveRetryCount();
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, record.getReceivedAt());
                statement.setString(2, record.getIngressType());
                statement.setString(3, record.getPayloadBase64());
                statement.setString(4, record.getPayloadHash());
                statement.setString(5, record.getSourceIdHash());
                statement.setString(6, record.getContentType());
                statement.setString(7, record.getScanStatus());
                statement.setString(8, record.getScanEngine());
                statement.setString(9, record.getScanSignature());
                if (record.getScanDurationMs() == null) {
                    statement.setObject(10, null);
                } else {
                    statement.setLong(10, record.getScanDurationMs());
                }
                statement.setString(11, record.getQuarantineReason());
                statement.executeUpdate();

                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        record.setId(keys.getLong(1));
                    }
                }
                return record;
            } catch (SQLException ex) {
                if (attempt == maxAttempts) {
                    throw new IllegalStateException("UNKNOWN_INGEST_SAVE_FAILED:Unknown Ingest 저장에 실패했습니다.", ex);
                }
                sleepRetry(attempt);
            }
        }
        return record;
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
