package com.mes.ai.service.impl;

import com.mes.ai.model.Envelope;
import com.mes.ai.model.RawEnvelope;
import com.mes.ai.service.StoreService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 메모리 기반 저장 구현입니다.
 * 목적: 초기 개발/테스트 단계에서 빠르게 저장 동작을 확인합니다.
 * 기능: 원본과 표준 데이터를 메모리 리스트에 보관합니다.
 * 이유: DB 연동 전에도 파이프라인 검증이 가능하게 합니다.
 */
public class InMemoryStoreService implements StoreService {
    /** 원본 데이터 저장소(스레드 안전)입니다. */
    private final List<RawEnvelope> rawStore = Collections.synchronizedList(new ArrayList<>());
    /** 표준 데이터 저장소(스레드 안전)입니다. */
    private final List<Envelope> standardStore = Collections.synchronizedList(new ArrayList<>());
    /** 원본 데이터 식별자 시퀀스입니다. */
    private final AtomicLong rawIdSequence = new AtomicLong(1);

    @Override
    public void storeRaw(RawEnvelope rawEnvelope) {
        // null 데이터는 저장하지 않습니다.
        if (rawEnvelope != null) {
            // 원본 ID가 없다면 메모리 시퀀스로 부여합니다.
            if (rawEnvelope.getId() == null) {
                rawEnvelope.setId(rawIdSequence.getAndIncrement());
            }
            rawStore.add(rawEnvelope);
        }
    }

    @Override
    public void storeStandard(Envelope envelope) {
        // 검증된 표준 데이터만 저장한다는 전제를 지킵니다.
        if (envelope != null) {
            standardStore.add(envelope);
        }
    }

    /**
     * 원본 데이터 리스트를 반환합니다.
     * 목적: 테스트/검증 시 저장 결과를 확인합니다.
     */
    public List<RawEnvelope> getRawStore() {
        return rawStore;
    }

    /**
     * 표준 데이터 리스트를 반환합니다.
     * 목적: 정상 처리 결과를 확인합니다.
     */
    public List<Envelope> getStandardStore() {
        return standardStore;
    }
}
