package com.mes.ai.pipeline.impl;

import com.mes.ai.model.ClassificationResult;
import com.mes.ai.model.EnvelopeCandidate;
import com.mes.ai.pipeline.Classifier;

import java.util.Map;

/**
 * payload 내 deviceTypeId를 기반으로 최소 분류를 수행합니다.
 * 목적: 장비 유형을 빠르게 식별해 후속 검증 규칙을 적용합니다.
 * 기능: payload에서 deviceTypeId를 찾아 ClassificationResult를 생성합니다.
 * 이유: 초기 단계에서는 간단한 규칙 기반 분류가 유지보수에 유리합니다.
 */
public class BasicClassifier implements Classifier {
    @Override
    public ClassificationResult classify(EnvelopeCandidate candidate) {
        // 결과 객체를 먼저 생성해 항상 반환합니다.
        ClassificationResult result = new ClassificationResult();
        if (candidate == null || candidate.getNormalizedPayload() == null) {
            // 입력이 없으면 분류 신뢰도를 0으로 두고 종료합니다.
            result.setConfidence(0.0);
            return result;
        }
        Map<String, Object> payload = candidate.getNormalizedPayload();
        // 다양한 키 이름을 고려하여 deviceTypeId를 찾습니다.
        Object value = payload.get("deviceTypeId");
        if (value == null) {
            value = payload.get("device_type_id");
        }
        if (value == null) {
            // 값이 없으면 분류 불가로 처리합니다.
            result.setConfidence(0.0);
            return result;
        }
        String deviceTypeId = String.valueOf(value).trim();
        if (deviceTypeId.isEmpty()) {
            // 공백이면 유효한 식별자가 아니므로 실패 처리합니다.
            result.setConfidence(0.0);
            return result;
        }
        // 식별자와 기본 신뢰도를 설정합니다.
        result.setDeviceTypeId(deviceTypeId);
        result.setConfidence(0.7);
        return result;
    }
}
