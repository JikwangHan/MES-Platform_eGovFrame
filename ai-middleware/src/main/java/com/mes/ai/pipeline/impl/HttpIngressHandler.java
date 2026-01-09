package com.mes.ai.pipeline.impl;

import com.mes.ai.model.RawEnvelope;
import com.mes.ai.pipeline.IngressHandler;
import com.mes.ai.util.Base64Utils;
import com.mes.ai.util.HashUtils;
import com.mes.ai.util.TimeUtils;

/**
 * HTTP 수신 기본 구현입니다.
 * 목적: HTTP로 수신한 원본 payload를 RawEnvelope로 변환합니다.
 * 기능: 수신 시각, ingress 유형, 해시, Base64 원본을 생성합니다.
 * 이유: 원본 보존과 추적성을 확보하기 위함입니다.
 * 유지보수: 헤더/테넌트 식별 규칙이 변경되면 이 클래스에 반영합니다.
 */
public class HttpIngressHandler implements IngressHandler {
    /** 장비/게이트웨이 식별자(해시 생성용)입니다. */
    private final String sourceId;
    /** 수신 payload의 MIME 타입입니다. */
    private final String contentType;

    /**
     * 목적: 소스와 contentType을 명시적으로 설정합니다.
     * 이유: HTTP 요청 헤더 정보를 보존하기 위함입니다.
     */
    public HttpIngressHandler(String sourceId, String contentType) {
        this.sourceId = sourceId;
        this.contentType = contentType;
    }

    /**
     * 목적: 수신 문자열을 RawEnvelope로 변환합니다.
     * 기능: 수신 시각/경로/해시/원본(Base64)을 설정합니다.
     * 이유: 원본 보관 정책과 무결성 추적을 만족하기 위함입니다.
     * 유지보수: 신규 메타데이터 추가 시 RawEnvelope 필드 확장과 함께 수정합니다.
     */
    @Override
    public RawEnvelope receive(String payload) {
        // payload가 null이면 원본 보관을 위해 빈 문자열로 치환합니다.
        String safePayload = payload == null ? "" : payload;

        RawEnvelope rawEnvelope = new RawEnvelope();
        // 수신 시각은 UTC ISO-8601 기준으로 기록합니다.
        rawEnvelope.setReceivedAt(TimeUtils.nowIsoUtc());
        // 어떤 경로로 수신되었는지 명확히 남깁니다.
        rawEnvelope.setIngressType("http");
        // 컨텐츠 타입을 기록하여 후속 파싱에 활용합니다.
        rawEnvelope.setContentType(contentType);
        // sourceId는 개인정보 보호를 위해 해시로 저장합니다.
        rawEnvelope.setSourceIdHash(HashUtils.sha256Hex(sourceId));
        // 원본 payload는 변조 방지를 위해 Base64로 보관합니다.
        rawEnvelope.setPayloadBase64(Base64Utils.encode(safePayload));
        // 원본 무결성 확인을 위해 해시를 함께 보관합니다.
        rawEnvelope.setPayloadHash(HashUtils.sha256Hex(safePayload));
        return rawEnvelope;
    }
}
