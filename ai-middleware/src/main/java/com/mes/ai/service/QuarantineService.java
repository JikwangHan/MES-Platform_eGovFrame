package com.mes.ai.service;

import com.mes.ai.model.RawEnvelope;
import com.mes.ai.model.ValidationResult;

/**
 * 격리(Quarantine) 처리 서비스입니다.
 * 실패 사유를 함께 저장하여 재처리에 활용합니다.
 */
public interface QuarantineService {
    void quarantine(RawEnvelope rawEnvelope, ValidationResult validationResult);
}
