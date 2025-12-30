package com.mes.ai.pipeline;

import com.mes.ai.model.RawEnvelope;

/**
 * 데이터 수신 단계 인터페이스입니다.
 * 목적: 외부에서 들어오는 원본 데이터를 일관된 형식으로 수신합니다.
 * 기능: payload를 받아 RawEnvelope로 변환합니다.
 * 이유: 이후 단계가 통일된 입력을 사용할 수 있도록 하기 위함입니다.
 */
public interface IngressHandler {
    /**
     * 원본 payload를 수신하여 RawEnvelope로 변환합니다.
     * 목적: 수신 시각, 전송 경로, 해시 등 기본 메타 정보를 생성합니다.
     */
    RawEnvelope receive(String payload);
}
