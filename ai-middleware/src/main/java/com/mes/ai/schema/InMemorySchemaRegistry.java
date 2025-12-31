package com.mes.ai.schema;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 메모리 기반 스키마 레지스트리입니다.
 * 목적: 초기 개발/테스트 단계에서 간단히 스키마를 조회합니다.
 * 기능: SchemaKey로 스키마 JSON을 저장/조회합니다.
 * 이유: DB 연동 전에도 스키마 검증 흐름을 확인할 수 있습니다.
 */
public class InMemorySchemaRegistry implements SchemaRegistry {
    private final Map<SchemaKey, String> schemaStore;

    /**
     * 목적: 초기 스키마 맵을 주입받습니다.
     * 이유: 테스트 환경에서 빠르게 스키마를 준비하기 위함입니다.
     */
    public InMemorySchemaRegistry(Map<SchemaKey, String> schemaStore) {
        if (schemaStore == null) {
            this.schemaStore = Collections.emptyMap();
            return;
        }
        this.schemaStore = Collections.unmodifiableMap(new HashMap<>(schemaStore));
    }

    /**
     * 목적: 스키마 키로 JSON 스키마를 조회합니다.
     * 이유: Validator가 대상 스키마를 선택할 수 있게 합니다.
     */
    @Override
    public String findSchema(SchemaKey key) {
        return schemaStore.get(key);
    }
}
