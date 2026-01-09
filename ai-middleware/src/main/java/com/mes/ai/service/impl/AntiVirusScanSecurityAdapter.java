package com.mes.ai.service.impl;

import com.mes.ai.model.AntiVirusScanResult;
import com.mes.ai.model.AntiVirusVerdict;
import com.mes.ai.model.InboundObject;
import com.mes.ai.model.ScanRequest;
import com.mes.ai.model.ScanResult;
import com.mes.ai.model.ScanStatus;
import com.mes.ai.service.AntiVirusScanService;
import com.mes.ai.service.SecurityScanService;

/**
 * 기존 SecurityScanService를 AntiVirusScanService로 연결하는 어댑터입니다.
 * 목적: 기존 스캔 구현체를 새 인터페이스와 호환되게 만듭니다.
 * 기능: ScanStatus를 AntiVirusVerdict로 변환합니다.
 * 이유: 코드 변경을 최소화하면서 설계안을 반영하기 위함입니다.
 * 유지보수: 스캔 결과 매핑 규칙 변경 시 이 클래스에서 조정합니다.
 */
public class AntiVirusScanSecurityAdapter implements AntiVirusScanService {
    private final SecurityScanService securityScanService;

    /**
     * 목적: 기존 스캔 서비스 구현체를 주입받습니다.
     * 기능: SecurityScanService를 내부 필드에 저장합니다.
     * 이유: 운영 환경에서 실제 엔진 구현체를 그대로 재사용하기 위함입니다.
     * 유지보수: 스캔 서비스 교체 시 생성자에서 주입만 변경합니다.
     */
    public AntiVirusScanSecurityAdapter(SecurityScanService securityScanService) {
        this.securityScanService = securityScanService;
    }

    /**
     * 목적: 인바운드 객체를 스캔하고 결과를 표준화합니다.
     * 기능: ScanRequest로 변환 후 스캔을 실행하고 결과를 매핑합니다.
     * 이유: 파이프라인이 AntiVirusScanResult 기준으로 동작하기 때문입니다.
     * 유지보수: 스캔 입력/출력 규칙 변경 시 이 메서드를 수정합니다.
     */
    @Override
    public AntiVirusScanResult scan(InboundObject inboundObject) {
        ScanRequest request = buildRequest(inboundObject);
        ScanResult scanResult = securityScanService.scan(request);
        return mapResult(scanResult);
    }

    /**
     * 목적: 인바운드 객체를 스캔 요청으로 변환합니다.
     * 기능: payload/base64/hash/contentType을 ScanRequest에 설정합니다.
     * 이유: 기존 스캔 서비스와 입력 포맷을 맞추기 위함입니다.
     * 유지보수: 스캔 입력 필드가 늘어나면 이 메서드를 수정합니다.
     */
    private ScanRequest buildRequest(InboundObject inboundObject) {
        ScanRequest request = new ScanRequest();
        if (inboundObject == null) {
            return request;
        }
        request.setPayloadBase64(inboundObject.getPayloadBase64());
        request.setPayloadHash(inboundObject.getPayloadHash());
        request.setContentType(inboundObject.getContentType());
        return request;
    }

    /**
     * 목적: 기존 스캔 결과를 표준 결과로 변환합니다.
     * 기능: ScanStatus를 AntiVirusVerdict로 매핑합니다.
     * 이유: OK/FOUND/ERROR 기준을 일관되게 적용하기 위함입니다.
     * 유지보수: 상태 매핑 규칙 변경 시 이 메서드를 수정합니다.
     */
    private AntiVirusScanResult mapResult(ScanResult scanResult) {
        AntiVirusScanResult result = new AntiVirusScanResult();
        if (scanResult == null || scanResult.getStatus() == null) {
            result.setVerdict(AntiVirusVerdict.ERROR);
            result.setErrorMessage("스캔 결과가 비어 있습니다.");
            return result;
        }
        result.setEngine(scanResult.getEngine());
        if (scanResult.getDurationMs() != null) {
            result.setDurationMs(scanResult.getDurationMs());
        }
        result.setThreatName(scanResult.getSignature());
        result.setSignatureVersion(scanResult.getEngine());

        ScanStatus status = scanResult.getStatus();
        if (status == ScanStatus.CLEAN) {
            result.setVerdict(AntiVirusVerdict.OK);
        } else if (status == ScanStatus.INFECTED) {
            result.setVerdict(AntiVirusVerdict.FOUND);
        } else {
            result.setVerdict(AntiVirusVerdict.ERROR);
            result.setErrorMessage(scanResult.getError());
        }
        return result;
    }
}
