package com.mes.ai.schema;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JDBC 기반 스키마 레지스트리입니다.
 * 목적: DB에 저장된 JSON Schema를 조회하여 검증 단계에 제공합니다.
 * 기능: schemaVersion/messageType/deviceTypeId 기준으로 스키마 본문을 반환합니다.
 * 이유: 운영 환경에서 스키마를 코드 수정 없이 관리하기 위함입니다.
 * 유지보수: 테이블/컬럼 구조 변경 시 SQL과 매핑을 이 클래스에서 수정합니다.
 */
public class JdbcSchemaRegistry implements SchemaRegistry {
    /**
     * 기본 조회 SQL입니다.
     * 목적: mapping_rules 테이블의 rule_body를 조회합니다.
     * 이유: 설계 문서에서 DB 스키마 저장 위치로 언급된 구조를 반영합니다.
     * 주의: 실제 컬럼명이 다르면 시스템 속성으로 SQL을 재정의해야 합니다.
     */
    private static final String DEFAULT_QUERY =
            "SELECT rule_body FROM mapping_rules " +
            "WHERE schema_version = ? AND message_type = ? AND device_type_id = ?";
    /**
     * 기본 폴백 조회 SQL입니다.
     * 목적: deviceTypeId가 없는 공용 스키마를 조회합니다.
     * 이유: 장비별 스키마가 없을 때 최소한의 검증을 수행합니다.
     */
    private static final String DEFAULT_FALLBACK_QUERY =
            "SELECT rule_body FROM mapping_rules " +
            "WHERE schema_version = ? AND message_type = ? AND device_type_id IS NULL";

    /** JDBC 연결을 제공하는 DataSource입니다. */
    private final DataSource dataSource;

    /**
     * 목적: DataSource를 주입받아 DB 조회를 수행합니다.
     * 이유: 연결 설정을 외부로 분리해 유지보수를 쉽게 합니다.
     */
    public JdbcSchemaRegistry(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 목적: 스키마 키로 스키마 본문을 조회합니다.
     * 기능: 장비별 스키마 → 공용 스키마 순으로 조회합니다.
     * 이유: 장비별 정의가 없을 때도 기본 검증을 수행하기 위함입니다.
     * 유지보수: 조회 순서/정책이 바뀌면 이 메서드를 수정합니다.
     */
    @Override
    public String findSchema(SchemaKey key) {
        if (key == null) {
            return null;
        }
        String query = resolveQuery("ai.schema.jdbc.query", DEFAULT_QUERY);
        String fallbackQuery = resolveQuery("ai.schema.jdbc.fallbackQuery", DEFAULT_FALLBACK_QUERY);
        String schema = findWithQuery(query, key, true);
        if (schema != null) {
            return schema;
        }
        // 장비별 스키마가 없으면 공용 스키마를 확인합니다.
        return findWithQuery(fallbackQuery, key, false);
    }

    /**
     * 목적: 쿼리를 실행해 스키마 본문을 조회합니다.
     * 이유: 조회 로직을 공통화하여 중복을 줄입니다.
     */
    private String findWithQuery(String query, SchemaKey key, boolean useDeviceType) {
        if (query == null || query.trim().isEmpty()) {
            return null;
        }
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, key.getSchemaVersion());
            statement.setString(2, key.getMessageType());
            if (useDeviceType) {
                statement.setString(3, key.getDeviceTypeId());
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString(1);
                }
            }
            return null;
        } catch (SQLException ex) {
            throw new IllegalStateException("스키마 조회에 실패했습니다.", ex);
        }
    }

    /**
     * 목적: 시스템 속성으로 쿼리를 재정의합니다.
     * 이유: 테이블/컬럼명이 다른 환경에 대응하기 위함입니다.
     */
    private String resolveQuery(String propertyKey, String defaultQuery) {
        String value = System.getProperty(propertyKey);
        if (value == null || value.trim().isEmpty()) {
            return defaultQuery;
        }
        return value.trim();
    }
}
