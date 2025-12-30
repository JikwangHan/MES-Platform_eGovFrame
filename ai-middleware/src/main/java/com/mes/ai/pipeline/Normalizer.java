package com.mes.ai.pipeline;

import com.mes.ai.model.EnvelopeCandidate;
import com.mes.ai.model.RawEnvelope;

/**
 * 포맷/키 정규화 단계 인터페이스입니다.
 */
public interface Normalizer {
    EnvelopeCandidate normalize(RawEnvelope rawEnvelope);
}
