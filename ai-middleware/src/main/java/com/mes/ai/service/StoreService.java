package com.mes.ai.service;

import com.mes.ai.model.Envelope;
import com.mes.ai.model.RawEnvelope;

/**
 * 저장 단계 서비스입니다.
 * 목적: 원본 데이터와 표준 데이터를 분리 저장하여 추적성과 품질을 유지합니다.
 * 기능: 원본(raw)과 표준(standard) 저장을 각각 처리합니다.
 * 이유: 원본 보존과 표준 데이터 품질 관리를 동시에 달성하기 위함입니다.
 * 유지보수: 저장소 교체 시 구현체만 변경하면 됩니다.
 */
public interface StoreService {
    /**
     * 원본 데이터를 저장합니다.
     * 목적: 원본 보존과 사후 분석을 가능하게 합니다.
     * 기능: 원본 메시지의 메타데이터/페이로드를 저장합니다.
     * 이유: 재처리와 감사 추적에 원본이 필요하기 때문입니다.
     * 유지보수: 저장 필드 변경 시 구현체를 수정합니다.
     */
    void storeRaw(RawEnvelope rawEnvelope);

    /**
     * 검증된 표준 데이터를 저장합니다.
     * 목적: 후속 서비스가 신뢰 가능한 데이터만 사용하도록 합니다.
     * 기능: 표준 Envelope를 표준 저장소에 저장합니다.
     * 이유: 검증 통과 데이터만 활용하기 위함입니다.
     * 유지보수: 저장 포맷 변경 시 구현체를 수정합니다.
     */
    void storeStandard(Envelope envelope);
}
