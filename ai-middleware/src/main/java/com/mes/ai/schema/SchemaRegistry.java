package com.mes.ai.schema;

/**
 * 스키마 조회를 위한 레지스트리 인터페이스입니다.
 * 목적: 스키마 저장 위치(DB/파일)와 조회 로직을 분리합니다.
 * 기능: 키 기반으로 JSON 스키마 문자열을 반환합니다.
 * 이유: 구현 교체가 쉬운 구조를 만들기 위함입니다.
 */
public interface SchemaRegistry {
    /**
     * 스키마를 조회합니다.
     * 목적: Validator가 스키마를 사용해 검증하도록 지원합니다.
     */
    String findSchema(SchemaKey key);
}
