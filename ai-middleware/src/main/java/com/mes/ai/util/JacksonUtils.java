package com.mes.ai.util;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Jackson ObjectMapper를 공유합니다.
 * 목적: JSON 직렬화/역직렬화를 표준 라이브러리로 처리합니다.
 * 기능: 싱글턴 ObjectMapper를 제공합니다.
 * 이유: 설정 일관성과 성능을 확보하기 위함입니다.
 * 유지보수: 구조 변경 시 이 클래스에서 조정합니다.
 */
public final class JacksonUtils {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 목적: 유틸리티 클래스의 인스턴스화를 방지합니다.
     * 기능: 외부에서 생성자를 호출할 수 없게 합니다.
     * 이유: 모든 기능을 정적 메서드로 제공하기 위함입니다.
     */
    private JacksonUtils() {
    }

    /**
     * 목적: 공용 ObjectMapper를 제공합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 파서/직렬화가 동일한 설정을 사용하도록 합니다.
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
}
