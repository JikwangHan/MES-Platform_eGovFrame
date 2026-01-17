package com.mes.ai.service.impl;

import com.mes.ai.crypto.CryptoService;
import com.mes.ai.crypto.CryptoServiceFactory;
import com.mes.ai.model.RawEnvelope;
import com.mes.ai.model.ScanResult;
import com.mes.ai.model.ValidationResult;
import com.mes.ai.service.QuarantineService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * JDBC 기반 격리 저장 스켈레톤입니다.
 * 목적: 격리 데이터 DB 저장 로직의 확장 지점을 제공합니다.
 * 기능: 격리 저장 메서드 시그니처를 고정합니다.
 * 이유: 격리 저장 구현을 추후 교체해도 호출부를 유지합니다.
 * 유지보수: 테이블 구조 변경 시 SQL과 매핑을 이 클래스에서 수정합니다.
 */
public class JdbcQuarantineService implements QuarantineService {
    /** 기본 저장 모드 SQL입니다. */
    private static final String INSERT_QUARANTINE_SQL_BASIC =
            "INSERT INTO quarantine_data (raw_id, reason_code, reason_detail, created_at) " +
            "VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
    /** 확장 저장 모드 SQL입니다. */
    private static final String INSERT_QUARANTINE_SQL_EXTENDED =
            "INSERT INTO quarantine_data (raw_id, reason_code, reason_detail, scan_status, scan_engine, scan_signature, scan_duration_ms, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
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
    /** 암호화 서비스입니다. */
    private final CryptoService cryptoService = CryptoServiceFactory.getInstance();

    /**
     * 목적: DataSource를 주입받아 격리 저장을 수행합니다.
     * 기능: DataSource를 내부 필드에 저장합니다.
     * 이유: 연결 설정을 외부로 분리해 유지보수를 쉽게 합니다.
     * 유지보수: 멀티테넌트 분리 시 DataSource 교체로 대응합니다.
     */
    public JdbcQuarantineService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 목적: 격리 데이터를 DB에 저장합니다.
     * 기능: 원본 ID와 사유 코드를 quarantine_data 테이블에 기록합니다.
     * 이유: 검증 실패 데이터의 추적과 재처리를 위해 필요합니다.
     * 유지보수: 사유 컬럼 구조 변경 시 splitReason/SQL을 수정합니다.
     */
    @Override
    public void quarantine(RawEnvelope rawEnvelope, ValidationResult validationResult, ScanResult scanResult) {
        // 실제 환경에서는 quarantine_data 테이블에 저장합니다.
        // 보안 스캔 결과는 스키마 확정 후 별도 컬럼에 저장할 수 있도록 확장 지점으로 둡니다.
        if (rawEnvelope == null) {
            return;
        }
        if (rawEnvelope.getId() == null) {
            throw new IllegalStateException("raw_id가 없어 격리 데이터를 저장할 수 없습니다.");
        }
        String reason = validationResult == null ? "UNKNOWN_ERROR:검증 결과 없음" : validationResult.getReason();
        ReasonParts parts = splitReason(reason);
        int maxAttempts = resolveRetryCount();
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(resolveInsertSql())) {
                if (useExtendedMode()) {
                    bindExtended(statement, rawEnvelope, parts, scanResult);
                } else {
                    bindBasic(statement, rawEnvelope, parts);
                }
                statement.executeUpdate();
                return;
            } catch (SQLException ex) {
                if (attempt == maxAttempts) {
                    throw new IllegalStateException("QUARANTINE_FAILED:격리 데이터 저장에 실패했습니다.", ex);
                }
                sleepRetry(attempt);
            }
        }
    }

    /**
     * 목적: reason을 코드/상세로 분리합니다.
     * 기능: 콜론 구분 규칙으로 reasonCode/reasonDetail을 생성합니다.
     * 이유: DB 컬럼 구조에 맞게 저장하기 위함입니다.
     * 유지보수: 사유 포맷 변경 시 이 메서드를 수정합니다.
     */
    private ReasonParts splitReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            return new ReasonParts("UNKNOWN_ERROR", "사유 없음");
        }
        int index = reason.indexOf(':');
        if (index <= 0) {
            return new ReasonParts(reason.trim(), null);
        }
        String code = reason.substring(0, index).trim();
        String detail = reason.substring(index + 1).trim();
        return new ReasonParts(code.isEmpty() ? "UNKNOWN_ERROR" : code, detail);
    }

    /**
     * 목적: reason 분리 결과를 보관하는 내부 클래스입니다.
     * 기능: reasonCode와 reasonDetail을 묶어 전달합니다.
     * 이유: 코드와 상세를 명확히 구분하기 위함입니다.
     * 유지보수: 사유 구조 변경 시 필드를 확장합니다.
     */
    private static final class ReasonParts {
        private final String reasonCode;
        private final String reasonDetail;

        /**
         * 목적: 분리된 사유 코드를 보관하는 객체를 생성합니다.
         * 기능: reasonCode/reasonDetail을 내부 필드에 설정합니다.
         * 이유: 격리 저장 시 사유를 구조화해 전달하기 위함입니다.
         * 유지보수: 사유 포맷 확장 시 파라미터를 추가합니다.
         */
        private ReasonParts(String reasonCode, String reasonDetail) {
            this.reasonCode = reasonCode;
            this.reasonDetail = reasonDetail;
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
     * 목적: 격리 저장 SQL을 선택합니다.
     * 기능: 기본/확장 모드에 따라 SQL을 반환합니다.
     * 이유: 운영 환경의 컬럼 구성 차이를 흡수하기 위함입니다.
     * 유지보수: 컬럼 구조 변경 시 상수를 수정합니다.
     */
    private String resolveInsertSql() {
        if (useExtendedMode()) {
            return INSERT_QUARANTINE_SQL_EXTENDED;
        }
        return INSERT_QUARANTINE_SQL_BASIC;
    }

    /**
     * 목적: 기본 모드의 격리 저장 바인딩을 수행합니다.
     * 기능: raw_id/사유만 저장합니다.
     * 이유: 최소 컬럼 구성을 지원하기 위함입니다.
     * 유지보수: 기본 모드 컬럼이 늘어나면 이 메서드를 수정합니다.
     */
    private void bindBasic(PreparedStatement statement, RawEnvelope rawEnvelope, ReasonParts parts) throws SQLException {
        statement.setLong(1, rawEnvelope.getId());
        statement.setString(2, parts.reasonCode);
        statement.setString(3, cryptoService.encrypt(parts.reasonDetail, "quarantine.reasonDetail"));
    }

    /**
     * 목적: 확장 모드의 격리 저장 바인딩을 수행합니다.
     * 기능: 기본 컬럼 + 스캔 정보를 저장합니다.
     * 이유: 보안 스캔 추적을 강화하기 위함입니다.
     * 유지보수: 확장 모드 컬럼이 바뀌면 이 메서드를 수정합니다.
     */
    private void bindExtended(PreparedStatement statement, RawEnvelope rawEnvelope, ReasonParts parts, ScanResult scanResult)
            throws SQLException {
        statement.setLong(1, rawEnvelope.getId());
        statement.setString(2, parts.reasonCode);
        statement.setString(3, cryptoService.encrypt(parts.reasonDetail, "quarantine.reasonDetail"));
        if (scanResult != null) {
            statement.setString(4, scanResult.getStatus() == null ? null : scanResult.getStatus().name());
            statement.setString(5, scanResult.getEngine());
            statement.setString(6, cryptoService.encrypt(scanResult.getSignature(), "quarantine.scanSignature"));
            if (scanResult.getDurationMs() == null) {
                statement.setObject(7, null);
            } else {
                statement.setLong(7, scanResult.getDurationMs());
            }
        } else {
            statement.setObject(4, null);
            statement.setObject(5, null);
            statement.setObject(6, null);
            statement.setObject(7, null);
        }
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
}
