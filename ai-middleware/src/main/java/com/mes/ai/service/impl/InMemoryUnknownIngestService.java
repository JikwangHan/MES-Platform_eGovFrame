package com.mes.ai.service.impl;

import com.mes.ai.crypto.CryptoService;
import com.mes.ai.crypto.CryptoServiceFactory;
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
    /** 암호화 서비스입니다. */
    private final CryptoService cryptoService = CryptoServiceFactory.getInstance();

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
        UnknownIngestRecord copy = copyAndProtect(record);
        records.add(copy);
        return copy;
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

    /**
     * 목적: UnknownIngest 레코드를 암호화 적용 후 복사합니다.
     * 기능: 원문/사유/시그니처를 컨테이너 포맷으로 변환합니다.
     * 이유: 저장 구간 암호화 정책을 테스트 단계에서도 적용하기 위함입니다.
     * 유지보수: 암호화 대상 필드가 늘어나면 이 메서드를 수정합니다.
     */
    private UnknownIngestRecord copyAndProtect(UnknownIngestRecord record) {
        UnknownIngestRecord copy = new UnknownIngestRecord();
        copy.setId(record.getId());
        copy.setReceivedAt(record.getReceivedAt());
        copy.setIngressType(record.getIngressType());
        copy.setPayloadBase64(cryptoService.encrypt(record.getPayloadBase64(), "unknown_ingest.payloadBase64"));
        copy.setPayloadHash(record.getPayloadHash());
        copy.setSourceIdHash(record.getSourceIdHash());
        copy.setContentType(record.getContentType());
        copy.setScanStatus(record.getScanStatus());
        copy.setScanEngine(record.getScanEngine());
        copy.setScanSignature(cryptoService.encrypt(record.getScanSignature(), "unknown_ingest.scanSignature"));
        copy.setScanDurationMs(record.getScanDurationMs());
        copy.setQuarantineReason(cryptoService.encrypt(record.getQuarantineReason(), "unknown_ingest.quarantineReason"));
        copy.setCreatedAt(record.getCreatedAt());
        return copy;
    }
}
