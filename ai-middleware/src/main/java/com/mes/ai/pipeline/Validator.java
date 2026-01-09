package com.mes.ai.pipeline;

import com.mes.ai.model.ClassificationResult;
import com.mes.ai.model.EnvelopeCandidate;
import com.mes.ai.model.ValidationResult;

/**
 * 검증 단계 인터페이스입니다.
 * 목적: 필수 필드와 값 범위를 확인하여 품질을 보장합니다.
 * 기능: 후보 데이터와 분류 결과를 기반으로 ValidationResult를 반환합니다.
 * 이유: 잘못된 데이터가 저장/분석 단계로 흘러가는 것을 방지합니다.
 * 유지보수: 검증 규칙 변경 시 구현체만 수정합니다.
 */
public interface Validator {
    /**
     * 후보 데이터와 분류 결과를 검증합니다.
     * 목적: 통과 여부와 실패 사유를 명확히 기록합니다.
     * 기능: 필드/형식/신뢰도 기준을 적용해 결과를 반환합니다.
     * 이유: 실패 데이터는 격리되고 재처리 기준으로 사용되기 때문입니다.
     * 유지보수: 규칙 추가/제거는 구현체에서 관리합니다.
     */
    ValidationResult validate(EnvelopeCandidate candidate, ClassificationResult classificationResult);
}
