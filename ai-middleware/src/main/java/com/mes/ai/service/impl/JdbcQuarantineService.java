package com.mes.ai.service.impl;

import com.mes.ai.model.RawEnvelope;
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
 */
public class JdbcQuarantineService implements QuarantineService {
    private static final String INSERT_QUARANTINE_SQL =
            "INSERT INTO quarantine_data (raw_id, reason_code, reason_detail, created_at) " +
            "VALUES (?, ?, ?, CURRENT_TIMESTAMP)";

    /** JDBC 연결을 제공하는 DataSource입니다. */
    private final DataSource dataSource;

    /**
     * 목적: DataSource를 주입받아 격리 저장을 수행합니다.
     * 이유: 연결 설정을 외부로 분리해 유지보수를 쉽게 합니다.
     */
    public JdbcQuarantineService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void quarantine(RawEnvelope rawEnvelope, ValidationResult validationResult) {
        // 실제 환경에서는 quarantine_data 테이블에 저장합니다.
        if (rawEnvelope == null) {
            return;
        }
        if (rawEnvelope.getId() == null) {
            throw new IllegalStateException("raw_id가 없어 격리 데이터를 저장할 수 없습니다.");
        }
        String reason = validationResult == null ? "UNKNOWN_ERROR:검증 결과 없음" : validationResult.getReason();
        ReasonParts parts = splitReason(reason);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_QUARANTINE_SQL)) {
            statement.setLong(1, rawEnvelope.getId());
            statement.setString(2, parts.reasonCode);
            statement.setString(3, parts.reasonDetail);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("격리 데이터 저장에 실패했습니다.", ex);
        }
    }

    /**
     * 목적: reason을 코드/상세로 분리합니다.
     * 이유: DB 컬럼 구조에 맞게 저장하기 위함입니다.
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
     * 이유: 코드와 상세를 명확히 구분하기 위함입니다.
     */
    private static final class ReasonParts {
        private final String reasonCode;
        private final String reasonDetail;

        private ReasonParts(String reasonCode, String reasonDetail) {
            this.reasonCode = reasonCode;
            this.reasonDetail = reasonDetail;
        }
    }
}
