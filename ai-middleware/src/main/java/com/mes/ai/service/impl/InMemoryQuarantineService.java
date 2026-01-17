package com.mes.ai.service.impl;

import com.mes.ai.crypto.CryptoService;
import com.mes.ai.crypto.CryptoServiceFactory;
import com.mes.ai.model.QuarantineRecord;
import com.mes.ai.model.RawEnvelope;
import com.mes.ai.model.ScanResult;
import com.mes.ai.model.ScanStatus;
import com.mes.ai.model.ValidationResult;
import com.mes.ai.service.QuarantineService;
import com.mes.ai.util.TimeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 메모리 기반 격리 저장 구현입니다.
 * 목적: 실패 데이터를 빠르게 확인할 수 있게 합니다.
 * 기능: 격리 레코드를 메모리에 저장합니다.
 * 이유: DB 연동 전에도 실패 원인을 추적할 수 있습니다.
 * 유지보수: 운영 환경에서는 JDBC 구현체로 교체합니다.
 */
public class InMemoryQuarantineService implements QuarantineService {
    /** 격리 레코드 목록(스레드 안전)입니다. */
    private final List<QuarantineRecord> records = Collections.synchronizedList(new ArrayList<>());
    /** 암호화 서비스입니다. */
    private final CryptoService cryptoService = CryptoServiceFactory.getInstance();

    /**
     * 목적: 실패 데이터를 메모리에 격리 저장합니다.
     * 기능: 원본/사유/스캔 정보를 QuarantineRecord로 기록합니다.
     * 이유: 검증 실패 데이터를 추적하고 재처리하기 위함입니다.
     * 유지보수: 기록 필드가 늘어나면 이 메서드를 수정합니다.
     */
    @Override
    public void quarantine(RawEnvelope rawEnvelope, ValidationResult validationResult, ScanResult scanResult) {
        // 격리 기록은 반드시 사유와 시각을 포함해야 합니다.
        QuarantineRecord record = new QuarantineRecord();
        record.setRawEnvelope(rawEnvelope);
        record.setReason(cryptoService.encrypt(validationResult == null ? "검증 결과 없음" : validationResult.getReason(),
                "quarantine.reasonDetail"));
        record.setQuarantinedAt(TimeUtils.nowIsoUtc());
        applyScanInfo(record, scanResult);
        records.add(record);
    }

    /**
     * 격리 레코드 목록을 반환합니다.
     * 목적: 실패 원인 분석 및 테스트 확인용입니다.
     * 기능: 내부 리스트를 반환합니다.
     * 이유: 테스트 환경에서 격리 결과를 확인하기 위함입니다.
     * 유지보수: 반환 방식 변경 시 이 메서드를 수정합니다.
     */
    public List<QuarantineRecord> getRecords() {
        return records;
    }

    /**
     * 목적: 보안 스캔 정보를 격리 레코드에 반영합니다.
     * 기능: 스캔 상태/엔진/시그니처/시간 정보를 채웁니다.
     * 이유: 격리 사유와 스캔 결과를 함께 보관해 감사/재처리를 쉽게 합니다.
     * 유지보수: 스캔 필드 확장 시 이 메서드를 수정합니다.
     */
    private void applyScanInfo(QuarantineRecord record, ScanResult scanResult) {
        if (record == null || scanResult == null) {
            return;
        }
        ScanStatus status = scanResult.getStatus();
        if (status != null) {
            record.setScanStatus(status.name());
        }
        record.setScanEngine(scanResult.getEngine());
        record.setScanSignature(cryptoService.encrypt(scanResult.getSignature(), "quarantine.scanSignature"));
        record.setScanDurationMs(scanResult.getDurationMs());
        record.setScanError(scanResult.getError());
    }
}
