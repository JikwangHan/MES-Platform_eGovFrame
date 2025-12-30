package com.mes.ai.pipeline;

import com.mes.ai.model.ClassificationResult;
import com.mes.ai.model.EnvelopeCandidate;

/**
 * 장비/포맷 분류 단계 인터페이스입니다.
 */
public interface Classifier {
    ClassificationResult classify(EnvelopeCandidate candidate);
}
