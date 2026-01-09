package com.mes.ai.pipeline;

import com.mes.ai.model.ClassificationResult;
import com.mes.ai.model.EnvelopeCandidate;

/**
 * 장비/포맷 분류 단계 인터페이스입니다.
 * 목적: 장비 유형이나 데이터 포맷을 식별해 후속 검증 규칙을 결정합니다.
 * 기능: 후보 데이터를 받아 분류 결과를 반환합니다.
 * 이유: 장비별 규칙을 적용하기 위해 분류 정보가 필요합니다.
 * 유지보수: 규칙 기반/모델 기반 분류로 전환 시 구현체만 교체합니다.
 */
public interface Classifier {
    /**
     * 후보 데이터를 분석해 장비 유형/포맷을 추정합니다.
     * 목적: deviceTypeId와 신뢰도 같은 핵심 분류 정보를 제공합니다.
     * 기능: 후보 입력을 분석해 분류 결과를 생성합니다.
     * 이유: 검증/저장 단계가 분류 기준을 필요로 하기 때문입니다.
     * 유지보수: 분류 기준 변경 시 구현체에서 조정합니다.
     */
    ClassificationResult classify(EnvelopeCandidate candidate);
}
