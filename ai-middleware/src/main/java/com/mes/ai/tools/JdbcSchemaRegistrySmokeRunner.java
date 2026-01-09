package com.mes.ai.tools;

import com.mes.ai.schema.JdbcSchemaRegistry;
import com.mes.ai.schema.SchemaKey;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * JDBC 스키마 레지스트리 스모크 테스트 실행기입니다.
 * 목적: DB 연결과 스키마 조회 경로를 빠르게 검증합니다.
 * 기능: 시스템 속성 기반으로 DB에 접속해 스키마 본문을 조회합니다.
 * 이유: 운영 전 단계에서 스키마 조회 문제를 사전에 발견하기 위함입니다.
 * 유지보수: 확장/변경 시 이 클래스에서 정책을 조정합니다.
 */
public class JdbcSchemaRegistrySmokeRunner {
    /**
     * 목적: main 동작을 수행합니다.
     * 기능: 필요한 처리를 수행합니다.
     * 이유: 기능 흐름을 한 곳에서 담당하기 위함입니다.
     * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
     */
    public static void main(String[] args) {
        // 목적: 시스템 속성에서 DB 접속 정보를 읽습니다.
        // 이유: 코드 수정 없이 환경별 테스트를 가능하게 합니다.
        String url = getRequired("ai.schema.jdbc.url");
        String user = getOptional("ai.schema.jdbc.user");
        String password = getOptional("ai.schema.jdbc.password");

        // 목적: 스키마 조회 키를 구성합니다.
        // 이유: 실제 조회 조건을 테스트하기 위함입니다.
        String schemaVersion = getRequired("ai.schema.key.schemaVersion");
        String messageType = getRequired("ai.schema.key.messageType");
        String deviceTypeId = getOptional("ai.schema.key.deviceTypeId");

        DataSource dataSource = new SimpleDataSource(url, user, password);
        JdbcSchemaRegistry registry = new JdbcSchemaRegistry(dataSource);

        SchemaKey key = new SchemaKey(schemaVersion, messageType, deviceTypeId);
        String schema = registry.findSchema(key);

        // 목적: 조회 결과를 콘솔에 출력합니다.
        // 이유: 테스트 성공/실패를 초보자도 쉽게 확인하도록 합니다.
        if (schema == null || schema.trim().isEmpty()) {
            System.out.println("스키마 조회 결과: 없음");
        } else {
            System.out.println("스키마 조회 성공: length=" + schema.length());
        }
    }

    /**
     * 필수 속성을 조회합니다.
     * 목적: 누락 시 빠르게 실패하도록 강제합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 외부에서 상태를 확인할 수 있도록 하기 위함입니다.
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
     * 기능: 요청한 값을 반환합니다.
     * 이유: 외부에서 상태를 확인할 수 있도록 하기 위함입니다.
     */
    private static String getOptional(String key) {
        String value = System.getProperty(key);
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    /**
     * 최소 구현 DataSource입니다.
     * 목적: 외부 라이브러리 없이 JDBC 연결만 제공하기 위함입니다.
     * 기능: 필요한 동작을 수행합니다.
     * 이유: 스모크 테스트에 필요한 기능만 제한적으로 제공합니다.
     * 유지보수: 구조 변경 시 이 클래스에서 조정합니다.
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

        /**
         * 목적: 값을 조회합니다.
         * 기능: 현재 설정된 값을 반환합니다.
         * 이유: 외부에서 상태를 확인할 수 있도록 하기 위함입니다.
         */
        @Override
        public Connection getConnection() throws SQLException {
            return DriverManager.getConnection(url, user, password);
        }

        /**
         * 목적: 값을 조회합니다.
         * 기능: 현재 설정된 값을 반환합니다.
         * 이유: 외부에서 상태를 확인할 수 있도록 하기 위함입니다.
         */
        @Override
        public Connection getConnection(String username, String pwd) throws SQLException {
            return DriverManager.getConnection(url, username, pwd);
        }

        /**
         * 목적: 값을 조회합니다.
         * 기능: 현재 설정된 값을 반환합니다.
         * 이유: 외부에서 상태를 확인할 수 있도록 하기 위함입니다.
         */
        @Override
        public PrintWriter getLogWriter() {
            throw new UnsupportedOperationException("미지원 기능입니다.");
        }

        /**
         * 목적: 값을 설정합니다.
         * 기능: 전달받은 값을 내부 필드에 저장합니다.
         * 이유: 모델 상태를 갱신하기 위함입니다.
         */
        @Override
        public void setLogWriter(PrintWriter out) {
            throw new UnsupportedOperationException("미지원 기능입니다.");
        }

        /**
         * 목적: 값을 설정합니다.
         * 기능: 전달받은 값을 내부 필드에 저장합니다.
         * 이유: 모델 상태를 갱신하기 위함입니다.
         */
        @Override
        public void setLoginTimeout(int seconds) {
            throw new UnsupportedOperationException("미지원 기능입니다.");
        }

        /**
         * 목적: 값을 조회합니다.
         * 기능: 현재 설정된 값을 반환합니다.
         * 이유: 외부에서 상태를 확인할 수 있도록 하기 위함입니다.
         */
        @Override
        public int getLoginTimeout() {
            return 0;
        }

        /**
         * 목적: 값을 조회합니다.
         * 기능: 현재 설정된 값을 반환합니다.
         * 이유: 외부에서 상태를 확인할 수 있도록 하기 위함입니다.
         */
        @Override
        public Logger getParentLogger() {
            throw new UnsupportedOperationException("미지원 기능입니다.");
        }

        /**
         * 목적: unwrap 동작을 수행합니다.
         * 기능: 필요한 처리를 수행합니다.
         * 이유: 기능 흐름을 한 곳에서 담당하기 위함입니다.
         * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
         */
        @Override
        public <T> T unwrap(Class<T> iface) {
            throw new UnsupportedOperationException("미지원 기능입니다.");
        }

        /**
         * 목적: 조건/상태 여부를 조회합니다.
         * 기능: 현재 상태 여부를 반환합니다.
         * 이유: 로직 분기 기준을 제공하기 위함입니다.
         */
        @Override
        public boolean isWrapperFor(Class<?> iface) {
            return false;
        }
    }
}
