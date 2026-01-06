package com.mes.ai.service;

import com.mes.ai.model.ClassificationResult;
import com.mes.ai.model.Envelope;
import com.mes.ai.model.EnvelopeCandidate;
import com.mes.ai.model.RawEnvelope;
import com.mes.ai.model.ScanRequest;
import com.mes.ai.model.ScanResult;
import com.mes.ai.model.ScanStatus;
import com.mes.ai.model.UnknownIngestRecord;
import com.mes.ai.model.ValidationResult;
import com.mes.ai.pipeline.Classifier;
import com.mes.ai.pipeline.Normalizer;
import com.mes.ai.pipeline.Validator;

/**
 * 파이프라인 전체 흐름을 오케스트레이션하는 기본 클래스입니다.
 * 목적: 수신된 원본 데이터를 표준화/검증/저장/격리까지 일관된 순서로 처리합니다.
 * 기능: 각 단계 구현체를 주입받아 순차 실행하며, 실패 시 격리로 안전하게 분기합니다.
 * 이유: 파이프라인 흐름을 한 곳에서 관리하면 유지보수와 확장이 쉬워집니다.
 * 유지보수 포인트: 예외 처리, 로깅, 멱등성 처리를 여기에 집중적으로 추가합니다.
 */
public class PipelineOrchestrator {
    /** 기본 허용 콘텐츠 타입 힌트 목록입니다. */
    private static final String[] DEFAULT_ALLOWED_CONTENT_TYPES = {
            "json",
            "csv",
            "xml",
            "tsv",
            "text/tsv",
            "tab-separated-values",
            "text/plain"
    };
    /** 기본 허용 수신 경로 목록입니다. */
    private static final String[] DEFAULT_ALLOWED_INGRESS_TYPES = {
            "mqtt",
            "http",
            "opcua",
            "modbus",
            "file"
    };
    /** 정규화 단계 구현체입니다. */
    private final Normalizer normalizer;
    /** 분류 단계 구현체입니다. */
    private final Classifier classifier;
    /** 검증 단계 구현체입니다. */
    private final Validator validator;
    /** 저장 단계 구현체입니다. */
    private final StoreService storeService;
    /** 격리 단계 구현체입니다. */
    private final QuarantineService quarantineService;
    /** 보안 스캔 단계 구현체입니다. */
    private final SecurityScanService securityScanService;
    /** 미정의 수신 데이터 저장 구현체입니다. */
    private final UnknownIngestService unknownIngestService;

    /**
     * 오케스트레이터가 사용할 단계 구현체를 주입합니다.
     * 목적: 교체 가능한 구조로 만들어 테스트와 확장을 쉽게 합니다.
     */
    public PipelineOrchestrator(
            Normalizer normalizer,
            Classifier classifier,
            Validator validator,
            StoreService storeService,
            QuarantineService quarantineService,
            SecurityScanService securityScanService,
            UnknownIngestService unknownIngestService
    ) {
        this.normalizer = normalizer;
        this.classifier = classifier;
        this.validator = validator;
        this.storeService = storeService;
        this.quarantineService = quarantineService;
        this.securityScanService = securityScanService;
        this.unknownIngestService = unknownIngestService;
    }

    /**
     * 원본 데이터를 표준 파이프라인으로 처리합니다.
     * 목적: 원본 저장 -> 정규화 -> 분류 -> 검증 -> 저장/격리 순서를 보장합니다.
     * 기능: 검증 통과 시 표준 데이터 저장, 실패 시 격리로 분기합니다.
     * 이유: 원본은 항상 보관해야 하며, 실패 사유는 추적 가능해야 합니다.
     */
    public void process(RawEnvelope rawEnvelope) {
        processInternal(rawEnvelope, false);
    }

    /**
     * 보안 스캔이 이미 완료된 데이터 처리용 경로입니다.
     * 목적: 스캔 결과가 확정된 이후에는 중복 스캔을 피합니다.
     * 이유: 큐 기반 스캔 구조에서 병목과 중복 처리를 줄이기 위함입니다.
     */
    public void processAfterScan(RawEnvelope rawEnvelope) {
        processInternal(rawEnvelope, true);
    }

    /**
     * 공통 처리 로직입니다.
     * 목적: 스캔 여부에 따라 공통 흐름을 재사용합니다.
     * 이유: 중복 코드를 줄이고 유지보수를 쉽게 하기 위함입니다.
     */
    private void processInternal(RawEnvelope rawEnvelope, boolean skipScan) {
        if (rawEnvelope == null) {
            // 원본 자체가 없으면 즉시 격리하여 후속 오류를 막습니다.
            quarantineService.quarantine(null, failResult("INGRESS_PAYLOAD_EMPTY:원본 데이터가 없습니다."));
            return;
        }
        // 원본 데이터는 반드시 보관해야 하므로 가장 먼저 저장합니다.
        storeService.storeRaw(rawEnvelope);

        if (isEmptyPayload(rawEnvelope)) {
            // payload가 비어 있으면 파싱/검증 전에 격리합니다.
            quarantineService.quarantine(rawEnvelope, failResult("INGRESS_PAYLOAD_EMPTY:payload가 비어 있습니다."));
            return;
        }

        ScanResult scanResult = null;
        if (!skipScan) {
            // 보안 스캔은 파싱/정규화 전에 반드시 수행하여 악성 데이터를 차단합니다.
            scanResult = securityScanService.scan(buildScanRequest(rawEnvelope));
            if (isScanBlocked(scanResult)) {
                // 스캔 실패/감염 상태는 Unknown Ingest로 격리 저장합니다.
                unknownIngestService.save(buildUnknownRecord(rawEnvelope, scanResult, "SECURITY_SCAN_BLOCKED"));
                return;
            }
        }

        // 미정의 통신/비정형 데이터로 판단되면 파이프라인에 진입시키지 않습니다.
        if (isUnknownIngress(rawEnvelope)) {
            unknownIngestService.save(buildUnknownRecord(rawEnvelope, scanResult, "UNKNOWN_INGEST"));
            return;
        }

        // 정규화 -> 분류 -> 검증 순으로 데이터 품질을 확인합니다.
        EnvelopeCandidate candidate = normalizer.normalize(rawEnvelope);
        ClassificationResult classificationResult = classifier.classify(candidate);
        ValidationResult validationResult = validator.validate(candidate, classificationResult);

        if (validationResult.isPass()) {
            Envelope envelope = new Envelope();
            // 버전 값은 운영 환경에서 변경 가능해야 하므로 설정으로 분리 예정입니다.
            envelope.setProtocolVersion("1.0");
            envelope.setSchemaVersion("1.0");
            envelope.setMessageType(candidate.getMessageType());
            envelope.setDeviceId(classificationResult.getDeviceTypeId());
            envelope.setTimestamp(rawEnvelope.getReceivedAt());
            envelope.setPayload(candidate.getNormalizedPayload());
            // 원본과 표준의 연결을 유지하기 위해 rawId를 설정합니다.
            envelope.setRawId(rawEnvelope.getId());

            // 검증을 통과한 표준 데이터만 저장합니다.
            storeService.storeStandard(envelope);
            return;
        }

        // 실패 데이터는 사유와 함께 격리하여 재처리에 활용합니다.
        quarantineService.quarantine(rawEnvelope, validationResult);
    }

    /**
     * 목적: 보안 스캔 요청 객체를 생성합니다.
     * 이유: 스캔 엔진이 필요한 입력을 한 곳에서 표준화하기 위함입니다.
     */
    private ScanRequest buildScanRequest(RawEnvelope rawEnvelope) {
        ScanRequest request = new ScanRequest();
        if (rawEnvelope != null) {
            request.setRawId(rawEnvelope.getId());
            request.setPayloadBase64(rawEnvelope.getPayloadBase64());
            request.setPayloadHash(rawEnvelope.getPayloadHash());
            request.setContentType(rawEnvelope.getContentType());
        }
        return request;
    }

    /**
     * 목적: 보안 스캔 결과가 차단 대상인지 판단합니다.
     * 이유: 스캔 실패/감염 데이터는 파이프라인에 진입하면 안 됩니다.
     */
    private boolean isScanBlocked(ScanResult result) {
        if (result == null || result.getStatus() == null) {
            return true;
        }
        return result.getStatus() != ScanStatus.CLEAN;
    }

    /**
     * 목적: 미정의 통신/비정형 데이터 여부를 최소 규칙으로 판단합니다.
     * 이유: 정의되지 않은 입력은 안전을 위해 Unknown Ingest로 분기합니다.
     */
    private boolean isUnknownIngress(RawEnvelope rawEnvelope) {
        if (rawEnvelope == null) {
            return true;
        }
        if (rawEnvelope.getIngressType() == null || rawEnvelope.getIngressType().trim().isEmpty()) {
            return true;
        }
        if (!isAllowedIngressType(rawEnvelope.getIngressType())) {
            return true;
        }
        if (rawEnvelope.getContentType() == null || rawEnvelope.getContentType().trim().isEmpty()) {
            return true;
        }
        return !isAllowedContentType(rawEnvelope.getContentType());
    }

    private boolean isEmptyPayload(RawEnvelope rawEnvelope) {
        String payloadBase64 = rawEnvelope.getPayloadBase64();
        return payloadBase64 == null || payloadBase64.trim().isEmpty();
    }

    /**
     * 목적: 콘텐츠 타입이 허용 목록에 포함되는지 확인합니다.
     * 이유: JSON 외 포맷도 정상 처리할 수 있게 하기 위함입니다.
     */
    private boolean isAllowedContentType(String contentType) {
        if (contentType == null) {
            return false;
        }
        String normalized = contentType.toLowerCase();
        String[] allowedTypes = resolveAllowedList("ai.ingest.allowedContentTypes", DEFAULT_ALLOWED_CONTENT_TYPES);
        for (String allowed : allowedTypes) {
            if (normalized.contains(allowed)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 목적: 수신 경로가 허용 목록에 포함되는지 확인합니다.
     * 이유: 미승인 통신 경로는 Unknown Ingest로 분기하기 위함입니다.
     */
    private boolean isAllowedIngressType(String ingressType) {
        if (ingressType == null) {
            return false;
        }
        String normalized = ingressType.toLowerCase().trim();
        String[] allowedTypes = resolveAllowedList("ai.ingest.allowedIngressTypes", DEFAULT_ALLOWED_INGRESS_TYPES);
        for (String allowed : allowedTypes) {
            if (normalized.equals(allowed)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 목적: 시스템 속성으로 허용 목록을 재정의합니다.
     * 이유: 코드 수정 없이 운영 환경에서 정책을 바꿀 수 있게 합니다.
     */
    private String[] resolveAllowedList(String propertyKey, String[] defaults) {
        String raw = System.getProperty(propertyKey);
        if (raw == null || raw.trim().isEmpty()) {
            return defaults;
        }
        String[] parts = raw.split(",");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim().toLowerCase();
        }
        return parts;
    }

    private ValidationResult failResult(String reason) {
        ValidationResult result = new ValidationResult();
        result.setPass(false);
        result.setReason(reason);
        return result;
    }

    /**
     * 목적: Unknown Ingest 저장 레코드를 생성합니다.
     * 이유: 격리 저장 시 보안 스캔 결과와 사유를 함께 보관합니다.
     */
    private UnknownIngestRecord buildUnknownRecord(RawEnvelope rawEnvelope, ScanResult scanResult, String reason) {
        UnknownIngestRecord record = new UnknownIngestRecord();
        if (rawEnvelope != null) {
            record.setReceivedAt(rawEnvelope.getReceivedAt());
            record.setIngressType(rawEnvelope.getIngressType());
            record.setPayloadBase64(rawEnvelope.getPayloadBase64());
            record.setPayloadHash(rawEnvelope.getPayloadHash());
            record.setSourceIdHash(rawEnvelope.getSourceIdHash());
            record.setContentType(rawEnvelope.getContentType());
        }
        if (scanResult != null) {
            if (scanResult.getStatus() != null) {
                record.setScanStatus(scanResult.getStatus().name());
            }
            record.setScanEngine(scanResult.getEngine());
            record.setScanSignature(scanResult.getSignature());
            record.setScanDurationMs(scanResult.getDurationMs());
        }
        record.setQuarantineReason(reason);
        return record;
    }
}
