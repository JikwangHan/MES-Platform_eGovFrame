package com.mes.ai.service;

import com.mes.ai.model.Envelope;
import com.mes.ai.model.RawEnvelope;

/**
 * 저장 단계 서비스입니다.
 * 원본과 표준 데이터를 분리 저장하는 것을 전제로 합니다.
 */
public interface StoreService {
    void storeRaw(RawEnvelope rawEnvelope);
    void storeStandard(Envelope envelope);
}
