package com.mes.ai.service;

import com.mes.ai.model.RawEnvelope;
import com.mes.ai.model.ValidationResult;

/**
 * 격리(Quarantine) 처리 서비스입니다.
 * 목적: 실패 데이터를 안전하게 분리 보관하여 문제 분석과 재처리를 가능하게 합니다.
 * 기능: 원본 데이터와 실패 사유를 함께 저장합니다.
 * 이유: 실패 원인을 추적하고 품질 개선에 활용하기 위함입니다.
 */
public interface QuarantineService {
    /**
     * 실패 데이터를 격리 저장합니다.
     * 목적: 재처리 대상과 원인을 명확히 기록합니다.
     */
    void quarantine(RawEnvelope rawEnvelope, ValidationResult validationResult);
}
