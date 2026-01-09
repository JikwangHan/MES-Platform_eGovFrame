package com.mes.ai.pipeline;

import com.mes.ai.model.EnvelopeCandidate;
import com.mes.ai.model.RawEnvelope;

/**
 * 포맷/키 정규화 단계 인터페이스입니다.
 * 목적: 다양한 입력 포맷을 표준 키/값 구조로 변환합니다.
 * 기능: RawEnvelope를 받아 EnvelopeCandidate를 생성합니다.
 * 이유: 이후 분류/검증 단계의 일관성을 확보하기 위함입니다.
 * 유지보수: 포맷 추가 시 구현체만 늘리면 됩니다.
 */
public interface Normalizer {
    /**
     * RawEnvelope를 표준 후보 형태로 변환합니다.
     * 목적: 포맷 판별과 키 정규화를 수행합니다.
     * 기능: 원본 payload를 표준 Map 구조로 변환합니다.
     * 이유: 검증/저장 단계가 동일한 구조를 기대하기 때문입니다.
     * 유지보수: 키 매핑 규칙 변경은 구현체에서 조정합니다.
     */
    EnvelopeCandidate normalize(RawEnvelope rawEnvelope);
}
