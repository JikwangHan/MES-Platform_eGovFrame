package com.mes.ai.pipeline;

import com.mes.ai.model.RawEnvelope;

/**
 * 데이터 수신 단계 인터페이스입니다.
 */
public interface IngressHandler {
    RawEnvelope receive(String payload);
}
