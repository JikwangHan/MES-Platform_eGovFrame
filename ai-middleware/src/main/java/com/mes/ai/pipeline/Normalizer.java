package com.mes.ai.pipeline;

import com.mes.ai.model.EnvelopeCandidate;
import com.mes.ai.model.RawEnvelope;

/**
 * 포맷/키 정규화 단계 인터페이스입니다.
 * 목적: 다양한 입력 포맷을 표준 키/값 구조로 변환합니다.
 * 기능: RawEnvelope를 받아 EnvelopeCandidate를 생성합니다.
 * 이유: 이후 분류/검증 단계의 일관성을 확보하기 위함입니다.
 */
public interface Normalizer {
    /**
     * RawEnvelope를 표준 후보 형태로 변환합니다.
     * 목적: 포맷 판별과 키 정규화를 수행합니다.
     */
    EnvelopeCandidate normalize(RawEnvelope rawEnvelope);
}
