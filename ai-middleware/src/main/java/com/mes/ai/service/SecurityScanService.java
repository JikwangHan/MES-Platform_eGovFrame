package com.mes.ai.service;

import com.mes.ai.model.ScanRequest;
import com.mes.ai.model.ScanResult;

/**
 * 보안 스캔 서비스 인터페이스입니다.
 * 목적: 스캔 엔진 교체에도 호출부는 유지되게 합니다.
 * 기능: 원본 데이터에 대한 악성코드 스캔을 수행합니다.
 * 이유: 보안 정책을 일관되게 적용하기 위함입니다.
 */
public interface SecurityScanService {
    /**
     * 보안 스캔을 수행합니다.
     * 목적: 파싱/저장 전에 악성코드 여부를 확인합니다.
     * 이유: 감염 데이터의 유입을 차단하기 위함입니다.
     */
    ScanResult scan(ScanRequest request);
}
