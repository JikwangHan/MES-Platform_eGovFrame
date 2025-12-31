package com.mes.ai.service;

import com.mes.ai.model.UnknownIngestRecord;

/**
 * 미정의 통신/비정형 데이터 격리 저장 서비스입니다.
 * 목적: 안전한 격리 저장을 통해 운영 파이프라인을 보호합니다.
 * 기능: Unknown Ingest 레코드를 저장합니다.
 * 이유: 신규 프로토콜/포맷 분석의 근거를 확보하기 위함입니다.
 */
public interface UnknownIngestService {
    /**
     * 미정의 수신 데이터를 저장합니다.
     * 목적: 원문과 보안 정보를 함께 보관합니다.
     * 이유: 분석 및 재처리를 위해 필요한 메타를 유지합니다.
     */
    UnknownIngestRecord save(UnknownIngestRecord record);
}
