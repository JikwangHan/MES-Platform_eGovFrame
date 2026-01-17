package com.mes.ai.service.impl;

import com.mes.ai.crypto.CryptoService;
import com.mes.ai.crypto.CryptoServiceFactory;
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
 * 유지보수: 운영 저장소로 전환 시 Jdbc 구현체로 교체합니다.
 */
public class InMemoryStoreService implements StoreService {
    /** 원본 데이터 저장소(스레드 안전)입니다. */
    private final List<RawEnvelope> rawStore = Collections.synchronizedList(new ArrayList<>());
    /** 표준 데이터 저장소(스레드 안전)입니다. */
    private final List<Envelope> standardStore = Collections.synchronizedList(new ArrayList<>());
    /** 원본 데이터 식별자 시퀀스입니다. */
    private final AtomicLong rawIdSequence = new AtomicLong(1);
    /** 암호화 서비스입니다. */
    private final CryptoService cryptoService = CryptoServiceFactory.getInstance();

    /**
     * 목적: 원본 데이터를 메모리에 저장합니다.
     * 기능: 원본 ID를 생성하고 리스트에 추가합니다.
     * 이유: 원본 보존 정책을 테스트 단계에서도 지키기 위함입니다.
     * 유지보수: 저장 구조 변경 시 이 메서드를 수정합니다.
     */
    @Override
    public void storeRaw(RawEnvelope rawEnvelope) {
        // null 데이터는 저장하지 않습니다.
        if (rawEnvelope != null) {
            // 원본 ID가 없다면 메모리 시퀀스로 부여합니다.
            if (rawEnvelope.getId() == null) {
                rawEnvelope.setId(rawIdSequence.getAndIncrement());
            }
            rawStore.add(copyAndProtectRaw(rawEnvelope));
        }
    }

    /**
     * 목적: 표준 데이터를 메모리에 저장합니다.
     * 기능: 표준 Envelope를 리스트에 추가합니다.
     * 이유: 검증 통과 데이터만 저장되는지 확인하기 위함입니다.
     * 유지보수: 저장 구조 변경 시 이 메서드를 수정합니다.
     */
    @Override
    public void storeStandard(Envelope envelope) {
        // 검증된 표준 데이터만 저장한다는 전제를 지킵니다.
        if (envelope != null) {
            standardStore.add(copyAndProtectStandard(envelope));
        }
    }

    /**
     * 목적: 원본 데이터를 암호화 적용 후 복사본으로 보관합니다.
     * 기능: 저장 구간에서 필요한 필드만 암호화해 복사 객체를 생성합니다.
     * 이유: 파이프라인 처리 중 원본 객체를 변경하지 않기 위함입니다.
     * 유지보수: 암호화 대상 필드가 늘어나면 이 메서드를 수정합니다.
     */
    private RawEnvelope copyAndProtectRaw(RawEnvelope rawEnvelope) {
        RawEnvelope copy = new RawEnvelope();
        copy.setId(rawEnvelope.getId());
        copy.setReceivedAt(rawEnvelope.getReceivedAt());
        copy.setIngressType(rawEnvelope.getIngressType());
        copy.setPayloadBase64(cryptoService.encrypt(rawEnvelope.getPayloadBase64(), "raw_data.payloadBase64"));
        copy.setPayloadHash(cryptoService.encrypt(rawEnvelope.getPayloadHash(), "raw_data.payloadHash"));
        copy.setSourceIdHash(cryptoService.encrypt(rawEnvelope.getSourceIdHash(), "raw_data.sourceIdHash"));
        copy.setContentType(cryptoService.encrypt(rawEnvelope.getContentType(), "raw_data.contentType"));
        return copy;
    }

    /**
     * 목적: 표준 데이터를 암호화 적용 후 복사본으로 보관합니다.
     * 기능: payload/버전/식별자/시간 필드를 암호화한 복사 객체를 생성합니다.
     * 이유: 저장 시 암호화 정책을 테스트 단계에서도 동일하게 확인하기 위함입니다.
     * 유지보수: 암호화 대상 변경 시 이 메서드를 수정합니다.
     */
    private Envelope copyAndProtectStandard(Envelope envelope) {
        Envelope copy = new Envelope();
        copy.setRawId(envelope.getRawId());
        copy.setMessageType(envelope.getMessageType());
        copy.setPayload(envelope.getPayload());
        copy.setSchemaVersion(cryptoService.encrypt(envelope.getSchemaVersion(), "parsed_data.schemaVersion"));
        copy.setProtocolVersion(cryptoService.encrypt(envelope.getProtocolVersion(), "parsed_data.protocolVersion"));
        copy.setDeviceId(cryptoService.encrypt(envelope.getDeviceId(), "parsed_data.deviceId"));
        copy.setTimestamp(cryptoService.encrypt(envelope.getTimestamp(), "parsed_data.timestamp"));
        return copy;
    }

    /**
     * 원본 데이터 리스트를 반환합니다.
     * 목적: 테스트/검증 시 저장 결과를 확인합니다.
     * 기능: 내부 저장 리스트를 반환합니다.
     * 이유: 테스트 코드에서 저장 상태를 점검하기 위함입니다.
     * 유지보수: 반환 방식 변경 시 이 메서드를 수정합니다.
     */
    public List<RawEnvelope> getRawStore() {
        return rawStore;
    }

    /**
     * 표준 데이터 리스트를 반환합니다.
     * 목적: 정상 처리 결과를 확인합니다.
     * 기능: 내부 저장 리스트를 반환합니다.
     * 이유: 테스트 코드에서 저장 상태를 점검하기 위함입니다.
     * 유지보수: 반환 방식 변경 시 이 메서드를 수정합니다.
     */
    public List<Envelope> getStandardStore() {
        return standardStore;
    }
}
