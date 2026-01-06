package com.mes.ai.tools;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * JDBC 스키마 키 목록 조회 실행기입니다.
 * 목적: mapping_rules에서 조회 키 후보를 빠르게 확인합니다.
 * 기능: schema_version, message_type, device_type_id를 최신순으로 출력합니다.
 * 이유: 스키마 조회 키를 모르는 경우에도 테스트를 진행할 수 있게 합니다.
 */
public class JdbcSchemaRegistryKeyLister {
    /** 기본 조회 SQL입니다. */
    private static final String DEFAULT_QUERY =
            "SELECT schema_version, message_type, device_type_id " +
            "FROM mapping_rules ORDER BY id DESC LIMIT 10";

    public static void main(String[] args) {
        // 목적: 시스템 속성에서 DB 접속 정보를 읽습니다.
        // 이유: 코드 수정 없이 환경별 테스트를 가능하게 합니다.
        String url = getRequired("ai.schema.jdbc.url");
        String user = getOptional("ai.schema.jdbc.user");
        String password = getOptional("ai.schema.jdbc.password");
        String query = resolveQuery("ai.schema.jdbc.keyQuery", DEFAULT_QUERY);

        DataSource dataSource = new SimpleDataSource(url, user, password);
        printKeys(dataSource, query);
    }

    /**
     * 키 목록을 출력합니다.
     * 목적: 스키마 조회에 필요한 파라미터를 확인합니다.
     */
    private static void printKeys(DataSource dataSource, String query) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            int count = 0;
            while (resultSet.next()) {
                String schemaVersion = resultSet.getString(1);
                String messageType = resultSet.getString(2);
                String deviceTypeId = resultSet.getString(3);
                System.out.println("schemaVersion=" + schemaVersion
                        + ", messageType=" + messageType
                        + ", deviceTypeId=" + deviceTypeId);
                count++;
            }
            if (count == 0) {
                System.out.println("키 조회 결과: 없음");
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("스키마 키 조회에 실패했습니다.", ex);
        }
    }

    /**
     * 필수 속성을 조회합니다.
     * 목적: 누락 시 빠르게 실패하도록 강제합니다.
     */
    private static String getRequired(String key) {
        String value = System.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("필수 속성이 없습니다: " + key);
        }
        return value.trim();
    }

    /**
     * 선택 속성을 조회합니다.
     * 목적: 없을 경우 빈 문자열로 처리합니다.
     */
    private static String getOptional(String key) {
        String value = System.getProperty(key);
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    /**
     * 쿼리를 시스템 속성으로 재정의합니다.
     * 목적: 테이블/컬럼명이 다른 환경에 대응합니다.
     */
    private static String resolveQuery(String propertyKey, String defaultQuery) {
        String value = System.getProperty(propertyKey);
        if (value == null || value.trim().isEmpty()) {
            return defaultQuery;
        }
        return value.trim();
    }

    /**
     * 최소 구현 DataSource입니다.
     * 목적: 외부 라이브러리 없이 JDBC 연결만 제공하기 위함입니다.
     * 이유: 스모크 테스트에 필요한 기능만 제한적으로 제공합니다.
     */
    private static final class SimpleDataSource implements DataSource {
        private final String url;
        private final String user;
        private final String password;

        private SimpleDataSource(String url, String user, String password) {
            this.url = url;
            this.user = user;
            this.password = password;
        }

        @Override
        public Connection getConnection() throws SQLException {
            return DriverManager.getConnection(url, user, password);
        }

        @Override
        public Connection getConnection(String username, String pwd) throws SQLException {
            return DriverManager.getConnection(url, username, pwd);
        }

        @Override
        public PrintWriter getLogWriter() {
            throw new UnsupportedOperationException("미지원 기능입니다.");
        }

        @Override
        public void setLogWriter(PrintWriter out) {
            throw new UnsupportedOperationException("미지원 기능입니다.");
        }

        @Override
        public void setLoginTimeout(int seconds) {
            throw new UnsupportedOperationException("미지원 기능입니다.");
        }

        @Override
        public int getLoginTimeout() {
            return 0;
        }

        @Override
        public Logger getParentLogger() {
            throw new UnsupportedOperationException("미지원 기능입니다.");
        }

        @Override
        public <T> T unwrap(Class<T> iface) {
            throw new UnsupportedOperationException("미지원 기능입니다.");
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) {
            return false;
        }
    }
}
