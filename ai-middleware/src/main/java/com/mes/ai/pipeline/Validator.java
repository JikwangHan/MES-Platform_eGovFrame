package com.mes.ai.pipeline;

import com.mes.ai.model.ClassificationResult;
import com.mes.ai.model.EnvelopeCandidate;
import com.mes.ai.model.ValidationResult;

/**
 * 검증 단계 인터페이스입니다.
 */
public interface Validator {
    ValidationResult validate(EnvelopeCandidate candidate, ClassificationResult classificationResult);
}
