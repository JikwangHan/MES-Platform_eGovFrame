package com.mes.ai.service;

import com.mes.ai.model.ClassificationResult;
import com.mes.ai.model.Envelope;
import com.mes.ai.model.EnvelopeCandidate;
import com.mes.ai.model.RawEnvelope;
import com.mes.ai.model.ValidationResult;
import com.mes.ai.pipeline.Classifier;
import com.mes.ai.pipeline.Normalizer;
import com.mes.ai.pipeline.Validator;

/**
 * 파이프라인 전체 흐름을 오케스트레이션하는 기본 클래스입니다.
 * 실제 구현에서는 예외 처리, 로깅, 멱등성 처리를 추가해야 합니다.
 */
public class PipelineOrchestrator {
    private final Normalizer normalizer;
    private final Classifier classifier;
    private final Validator validator;
    private final StoreService storeService;
    private final QuarantineService quarantineService;

    public PipelineOrchestrator(
            Normalizer normalizer,
            Classifier classifier,
            Validator validator,
            StoreService storeService,
            QuarantineService quarantineService
    ) {
        this.normalizer = normalizer;
        this.classifier = classifier;
        this.validator = validator;
        this.storeService = storeService;
        this.quarantineService = quarantineService;
    }

    public void process(RawEnvelope rawEnvelope) {
        // 원본은 항상 저장합니다.
        storeService.storeRaw(rawEnvelope);

        EnvelopeCandidate candidate = normalizer.normalize(rawEnvelope);
        ClassificationResult classificationResult = classifier.classify(candidate);
        ValidationResult validationResult = validator.validate(candidate, classificationResult);

        if (validationResult.isPass()) {
            Envelope envelope = new Envelope();
            // 버전 값은 설정 파일 또는 환경 변수로 분리 예정입니다.
            envelope.setProtocolVersion("1.0");
            envelope.setSchemaVersion("1.0");
            envelope.setMessageType(candidate.getMessageType());
            envelope.setDeviceId(classificationResult.getDeviceTypeId());
            envelope.setTimestamp(rawEnvelope.getReceivedAt());
            envelope.setPayload(candidate.getNormalizedPayload());

            storeService.storeStandard(envelope);
            return;
        }

        quarantineService.quarantine(rawEnvelope, validationResult);
    }
}
