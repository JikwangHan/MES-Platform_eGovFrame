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
import java.util.Map;

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
     * 유지보수: 수신 경로가 늘어나면 앞단 Ingress에서 RawEnvelope만 맞추면 됩니다.
     */
    public void process(RawEnvelope rawEnvelope) {
        processInternal(rawEnvelope, false, null);
    }

    /**
     * 보안 스캔이 이미 완료된 데이터 처리용 경로입니다.
     * 목적: 스캔 결과가 확정된 이후에는 중복 스캔을 피합니다.
     * 이유: 큐 기반 스캔 구조에서 병목과 중복 처리를 줄이기 위함입니다.
     * 유지보수: 스캔/파이프라인 분리 정책이 바뀌면 이 경로를 조정합니다.
     */
    public void processAfterScan(RawEnvelope rawEnvelope) {
        processAfterScan(rawEnvelope, null);
    }

    /**
     * 보안 스캔 결과를 포함해 처리하는 경로입니다.
     * 목적: 스캔 결과를 Unknown 기록에 함께 남깁니다.
     * 이유: 큐 기반 스캔에서는 스캔과 파이프라인이 분리되어 있기 때문입니다.
     * 유지보수: 스캔 결과 포맷이 바뀌면 이 메서드에서 반영합니다.
     */
    public void processAfterScan(RawEnvelope rawEnvelope, ScanResult scanResult) {
        processInternal(rawEnvelope, true, scanResult);
    }

    /**
     * 공통 처리 로직입니다.
     * 목적: 스캔 여부에 따라 공통 흐름을 재사용합니다.
     * 이유: 중복 코드를 줄이고 유지보수를 쉽게 하기 위함입니다.
     * 유지보수: 핵심 파이프라인 정책 변경 시 이 메서드를 우선 수정합니다.
     */
    private void processInternal(RawEnvelope rawEnvelope, boolean skipScan, ScanResult scanSnapshot) {
        if (rawEnvelope == null) {
            // 원본 자체가 없으면 즉시 격리하여 후속 오류를 막습니다.
            quarantineService.quarantine(null, failResult("INGRESS_PAYLOAD_EMPTY:원본 데이터가 없습니다."), null);
            return;
        }
        // 원본 데이터는 반드시 보관해야 하므로 가장 먼저 저장합니다.
        storeService.storeRaw(rawEnvelope);

        if (isEmptyPayload(rawEnvelope)) {
            // payload가 비어 있으면 파싱/검증 전에 격리합니다.
            quarantineService.quarantine(rawEnvelope, failResult("INGRESS_PAYLOAD_EMPTY:payload가 비어 있습니다."), scanSnapshot);
            return;
        }

        ScanResult scanResult = scanSnapshot;
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
            // 경고성 통과는 표준 저장과 함께 Unknown 기록을 남겨 추적성을 확보합니다.
            if (isValidationWarning(validationResult)) {
                unknownIngestService.save(buildUnknownRecord(rawEnvelope, scanResult, validationResult.getReason()));
            }
            /*
             * 목적: 검증 통과 데이터를 표준 Envelope로 변환해 저장합니다.
             * 기능: payload에서 핵심 식별/버전/시간 정보를 추출하여 표준 필드에 매핑합니다.
             * 이유: 원본 의미를 유지한 채 후속 시스템이 일관된 구조로 사용할 수 있게 하기 위함입니다.
             * 유지보수: 키 별칭 추가/변경은 readPayloadValue 호출부만 조정하면 됩니다.
             */
            Envelope envelope = new Envelope();
            // 표준 필드는 정규화된 payload에서 추출하여 원본 의미를 유지합니다.
            Map<String, Object> payload = candidate.getNormalizedPayload();
            envelope.setProtocolVersion(readPayloadValue(payload, "protocolVersion", "protocol_version"));
            envelope.setSchemaVersion(readPayloadValue(payload, "schemaVersion", "schema_version"));
            envelope.setMessageType(candidate.getMessageType());
            envelope.setDeviceId(readPayloadValue(payload, "deviceId", "device_id"));
            // payload의 timestamp가 우선이며, 없으면 수신 시각으로 보완합니다.
            String payloadTimestamp = readPayloadValue(payload, "timestamp", "eventTime", "event_time");
            envelope.setTimestamp(payloadTimestamp != null ? payloadTimestamp : rawEnvelope.getReceivedAt());
            envelope.setPayload(payload);
            // 원본과 표준의 연결을 유지하기 위해 rawId를 설정합니다.
            envelope.setRawId(rawEnvelope.getId());

            // 검증을 통과한 표준 데이터만 저장합니다.
            storeService.storeStandard(envelope);
            return;
        }

        // 실패 데이터는 사유와 함께 격리하여 재처리에 활용합니다.
        quarantineService.quarantine(rawEnvelope, validationResult, scanResult);
    }

    /**
     * 목적: 경고성 통과 여부를 판단합니다.
     * 기능: reason 접두어를 기준으로 경고 상태를 판별합니다.
     * 이유: 스키마 미등록 경고처럼 통과하지만 추적이 필요한 경우를 분리합니다.
     * 유지보수: 경고 코드 규칙이 바뀌면 이 메서드를 수정합니다.
     */
    private boolean isValidationWarning(ValidationResult result) {
        if (result == null) {
            return false;
        }
        if (!result.isPass()) {
            return false;
        }
        String reason = result.getReason();
        if (reason == null) {
            return false;
        }
        return reason.startsWith("SCHEMA_MISSING_WARN:");
    }

    /**
     * 목적: 보안 스캔 요청 객체를 생성합니다.
     * 기능: RawEnvelope에서 스캔에 필요한 필드를 추출합니다.
     * 이유: 스캔 엔진이 필요한 입력을 한 곳에서 표준화하기 위함입니다.
     * 유지보수: 스캔 입력 필드가 추가되면 이 메서드를 확장합니다.
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
     * 기능: 결과가 없거나 CLEAN이 아니면 차단으로 봅니다.
     * 이유: 스캔 실패/감염 데이터는 파이프라인에 진입하면 안 됩니다.
     * 유지보수: 스캔 상태 정책이 바뀌면 이 메서드에서 조정합니다.
     */
    private boolean isScanBlocked(ScanResult result) {
        if (result == null || result.getStatus() == null) {
            return true;
        }
        return result.getStatus() != ScanStatus.CLEAN;
    }

    /**
     * 목적: 미정의 통신/비정형 데이터 여부를 최소 규칙으로 판단합니다.
     * 기능: ingress/contentType 허용 목록을 기준으로 판단합니다.
     * 이유: 정의되지 않은 입력은 안전을 위해 Unknown Ingest로 분기합니다.
     * 유지보수: 허용 목록 정책 변경 시 이 메서드를 수정합니다.
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

    /**
     * 목적: payload가 비어 있는지 확인합니다.
     * 기능: Base64 문자열의 null/공백 여부를 검사합니다.
     * 이유: 빈 payload는 파싱 오류로 이어지기 때문입니다.
     * 유지보수: payload 저장 방식이 바뀌면 이 메서드를 수정합니다.
     */
    private boolean isEmptyPayload(RawEnvelope rawEnvelope) {
        String payloadBase64 = rawEnvelope.getPayloadBase64();
        return payloadBase64 == null || payloadBase64.trim().isEmpty();
    }

    /**
     * 목적: payload에서 키 후보에 해당하는 값을 문자열로 추출합니다.
     * 기능: 여러 키 후보를 순회해 첫 번째 유효 값을 반환합니다.
     * 이유: 표준 필드가 다양한 키 이름으로 들어오는 경우를 흡수합니다.
     * 유지보수: 키 체계가 바뀌면 호출부에 별칭만 추가하면 됩니다.
     */
    private String readPayloadValue(Map<String, Object> payload, String... keys) {
        if (payload == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            if (key == null) {
                continue;
            }
            Object value = payload.get(key);
            if (value == null) {
                continue;
            }
            String text = String.valueOf(value).trim();
            if (!text.isEmpty()) {
                return text;
            }
        }
        return null;
    }

    /**
     * 목적: 콘텐츠 타입이 허용 목록에 포함되는지 확인합니다.
     * 이유: JSON 외 포맷도 정상 처리할 수 있게 하기 위함입니다.
     * 기능: 시스템 속성 재정의 목록을 사용해 검사합니다.
     * 유지보수: 포맷 정책 변경 시 허용 목록을 조정합니다.
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
     * 기능: 시스템 속성 재정의 목록을 사용해 검사합니다.
     * 유지보수: 수신 경로 정책 변경 시 허용 목록을 조정합니다.
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
     * 기능: 시스템 속성 값을 읽어 소문자 배열로 반환합니다.
     * 유지보수: 속성 키가 변경되면 이 메서드 호출부를 수정합니다.
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

    /**
     * 목적: 실패 ValidationResult를 생성합니다.
     * 기능: pass=false와 reason을 설정합니다.
     * 이유: 실패 사유를 항상 남기기 위함입니다.
     * 유지보수: 실패 모델 변경 시 이 메서드를 수정합니다.
     */
    private ValidationResult failResult(String reason) {
        ValidationResult result = new ValidationResult();
        result.setPass(false);
        result.setReason(reason);
        return result;
    }

    /**
     * 목적: Unknown Ingest 저장 레코드를 생성합니다.
     * 이유: 격리 저장 시 보안 스캔 결과와 사유를 함께 보관합니다.
     * 기능: 원본/스캔 정보를 UnknownIngestRecord로 변환합니다.
     * 유지보수: 저장 필드가 확장되면 이 메서드를 수정합니다.
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
