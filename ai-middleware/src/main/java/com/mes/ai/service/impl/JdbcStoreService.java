package com.mes.ai.service.impl;

import com.mes.ai.model.Envelope;
import com.mes.ai.model.RawEnvelope;
import com.mes.ai.service.StoreService;

/**
 * JDBC 기반 저장 스켈레톤입니다.
 * 목적: DB 저장 로직의 확장 지점을 제공합니다.
 * 기능: 원본/표준 저장 메서드 시그니처를 고정합니다.
 * 이유: 구현 교체 시에도 호출부를 변경하지 않기 위함입니다.
 */
public class JdbcStoreService implements StoreService {
    @Override
    public void storeRaw(RawEnvelope rawEnvelope) {
        // 실제 환경에서는 raw_data 테이블에 저장하도록 구현합니다.
        throw new UnsupportedOperationException("DB 저장 구현이 필요합니다.");
    }

    @Override
    public void storeStandard(Envelope envelope) {
        // 실제 환경에서는 parsed_data 테이블에 저장하도록 구현합니다.
        throw new UnsupportedOperationException("DB 저장 구현이 필요합니다.");
    }
}
