package com.mes.ai.service.impl;

import com.mes.ai.model.RawEnvelope;
import com.mes.ai.model.ValidationResult;
import com.mes.ai.service.QuarantineService;

/**
 * JDBC 기반 격리 저장 스켈레톤입니다.
 * 목적: 격리 데이터 DB 저장 로직의 확장 지점을 제공합니다.
 * 기능: 격리 저장 메서드 시그니처를 고정합니다.
 * 이유: 격리 저장 구현을 추후 교체해도 호출부를 유지합니다.
 */
public class JdbcQuarantineService implements QuarantineService {
    @Override
    public void quarantine(RawEnvelope rawEnvelope, ValidationResult validationResult) {
        // 실제 환경에서는 quarantine_data 테이블에 저장하도록 구현합니다.
        throw new UnsupportedOperationException("DB 격리 저장 구현이 필요합니다.");
    }
}
