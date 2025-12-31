package com.mes.ai.util;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Jackson ObjectMapper를 공유합니다.
 * 목적: JSON 직렬화/역직렬화를 표준 라이브러리로 처리합니다.
 * 기능: 싱글턴 ObjectMapper를 제공합니다.
 * 이유: 설정 일관성과 성능을 확보하기 위함입니다.
 */
public final class JacksonUtils {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** 유틸리티 클래스이므로 외부에서 인스턴스화하지 못하게 합니다. */
    private JacksonUtils() {
    }

    /**
     * 목적: 공용 ObjectMapper를 제공합니다.
     * 이유: 파서/직렬화가 동일한 설정을 사용하도록 합니다.
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
}
