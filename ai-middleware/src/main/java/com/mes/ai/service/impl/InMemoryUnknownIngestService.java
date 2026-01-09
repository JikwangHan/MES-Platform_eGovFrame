package com.mes.ai.service.impl;

import com.mes.ai.model.UnknownIngestRecord;
import com.mes.ai.service.UnknownIngestService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 미정의 수신 데이터를 메모리에 저장하는 구현체입니다.
 * 목적: 초기 테스트 단계에서 DB 없이 동작하도록 합니다.
 * 기능: 저장 시 식별자를 부여하고 목록에 보관합니다.
 * 이유: 구현 복잡도를 낮추고 빠르게 검증하기 위함입니다.
 * 유지보수: 운영 환경에서는 JDBC 구현체로 교체합니다.
 */
public class InMemoryUnknownIngestService implements UnknownIngestService {
    /** 메모리 저장소입니다. */
    private final List<UnknownIngestRecord> records = new ArrayList<>();
    /** 식별자 생성을 위한 시퀀스입니다. */
    private final AtomicLong sequence = new AtomicLong(1);

    /**
     * 목적: UnknownIngest 레코드를 메모리에 저장합니다.
     * 기능: 식별자를 부여한 뒤 목록에 추가합니다.
     * 이유: 테스트 단계에서도 격리 기록을 확인할 수 있어야 하기 때문입니다.
     * 유지보수: 저장 구조 변경 시 이 메서드를 수정합니다.
     */
    @Override
    public synchronized UnknownIngestRecord save(UnknownIngestRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("UnknownIngestRecord가 없습니다.");
        }
        if (record.getId() == null) {
            record.setId(sequence.getAndIncrement());
        }
        records.add(record);
        return record;
    }

    /**
     * 목적: 저장된 레코드를 조회합니다.
     * 기능: 내부 목록을 복사해 반환합니다.
     * 이유: 테스트에서 저장 여부를 확인하기 위함입니다.
     * 유지보수: 조회 방식 변경 시 이 메서드를 수정합니다.
     */
    public synchronized List<UnknownIngestRecord> getRecords() {
        return new ArrayList<>(records);
    }
}
