package com.mes.ai.service.impl;

import com.mes.ai.model.QuarantineRecord;
import com.mes.ai.model.RawEnvelope;
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
 */
public class InMemoryQuarantineService implements QuarantineService {
    /** 격리 레코드 목록(스레드 안전)입니다. */
    private final List<QuarantineRecord> records = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void quarantine(RawEnvelope rawEnvelope, ValidationResult validationResult) {
        // 격리 기록은 반드시 사유와 시각을 포함해야 합니다.
        QuarantineRecord record = new QuarantineRecord();
        record.setRawEnvelope(rawEnvelope);
        record.setReason(validationResult == null ? "검증 결과 없음" : validationResult.getReason());
        record.setQuarantinedAt(TimeUtils.nowIsoUtc());
        records.add(record);
    }

    /**
     * 격리 레코드 목록을 반환합니다.
     * 목적: 실패 원인 분석 및 테스트 확인용입니다.
     */
    public List<QuarantineRecord> getRecords() {
        return records;
    }
}
