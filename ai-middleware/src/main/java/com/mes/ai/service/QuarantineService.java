package com.mes.ai.service;

import com.mes.ai.model.RawEnvelope;
import com.mes.ai.model.ScanResult;
import com.mes.ai.model.ValidationResult;

/**
 * 격리(Quarantine) 처리 서비스입니다.
 * 목적: 실패 데이터를 안전하게 분리 보관하여 문제 분석과 재처리를 가능하게 합니다.
 * 기능: 원본 데이터와 실패 사유를 함께 저장합니다.
 * 이유: 실패 원인을 추적하고 품질 개선에 활용하기 위함입니다.
 * 유지보수: 격리 저장소 변경 시 구현체만 교체합니다.
 */
public interface QuarantineService {
    /**
     * 실패 데이터를 격리 저장합니다.
     * 목적: 재처리 대상과 원인을 명확히 기록합니다.
     * 기능: 스캔 결과 없이도 격리를 수행합니다.
     * 이유: 일부 경로에서는 스캔 결과가 없기 때문입니다.
     * 유지보수: 기본 처리 규칙 변경 시 구현체를 수정합니다.
     */
    default void quarantine(RawEnvelope rawEnvelope, ValidationResult validationResult) {
        // 스캔 결과가 없는 호출 경로를 통합하여 처리합니다.
        quarantine(rawEnvelope, validationResult, null);
    }

    /**
     * 실패 데이터를 격리 저장합니다.
     * 목적: 검증 실패와 보안 스캔 결과를 함께 기록합니다.
     * 이유: 보안 정책 감사와 재처리 판단을 위한 근거를 남깁니다.
     * 기능: 원본/검증 결과/스캔 결과를 함께 저장합니다.
     * 유지보수: 저장 필드 확장 시 구현체를 수정합니다.
     */
    void quarantine(RawEnvelope rawEnvelope, ValidationResult validationResult, ScanResult scanResult);
}
