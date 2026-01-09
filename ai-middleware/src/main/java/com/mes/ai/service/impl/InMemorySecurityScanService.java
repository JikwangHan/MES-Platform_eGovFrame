package com.mes.ai.service.impl;

import com.mes.ai.model.ScanRequest;
import com.mes.ai.model.ScanResult;
import com.mes.ai.model.ScanStatus;
import com.mes.ai.service.SecurityScanService;

/**
 * 보안 스캔 서비스의 최소 구현체입니다.
 * 목적: 실제 엔진 연동 전 테스트를 위한 기본 동작을 제공합니다.
 * 기능: 기본적으로 ERROR를 반환해 안전하게 차단합니다.
 * 이유: 스캔 엔진이 없는 상태에서 CLEAN을 반환하면 보안 사고 위험이 있습니다.
 * 유지보수: 테스트 정책 변경 시 시스템 속성 규칙을 조정합니다.
 */
public class InMemorySecurityScanService implements SecurityScanService {
    /**
     * 목적: 테스트 환경에서 스캔 결과를 생성합니다.
     * 기능: 설정에 따라 CLEAN 또는 ERROR 결과를 반환합니다.
     * 이유: 엔진 미연동 상태에서 안전하게 차단하기 위함입니다.
     * 유지보수: 테스트 플래그 키가 바뀌면 이 메서드를 수정합니다.
     */
    @Override
    public ScanResult scan(ScanRequest request) {
        // 테스트 환경에서는 시스템 속성으로 CLEAN 반환 여부를 제어합니다.
        if (Boolean.parseBoolean(System.getProperty("ai.security.scan.mockClean", "false"))) {
            ScanResult clean = new ScanResult();
            clean.setStatus(ScanStatus.CLEAN);
            clean.setEngine("IN_MEMORY");
            return clean;
        }
        // 실제 스캔 엔진이 없으므로 오류 상태로 반환해 파이프라인 진행을 막습니다.
        ScanResult result = new ScanResult();
        result.setStatus(ScanStatus.ERROR);
        result.setEngine("IN_MEMORY");
        result.setError("보안 스캔 엔진 미연동 상태");
        return result;
    }
}
