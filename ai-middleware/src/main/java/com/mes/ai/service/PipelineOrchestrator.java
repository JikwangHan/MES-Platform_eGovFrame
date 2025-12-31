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
 * 목적: 수신된 원본 데이터를 표준화/검증/저장/격리까지 일관된 순서로 처리합니다.
 * 기능: 각 단계 구현체를 주입받아 순차 실행하며, 실패 시 격리로 안전하게 분기합니다.
 * 이유: 파이프라인 흐름을 한 곳에서 관리하면 유지보수와 확장이 쉬워집니다.
 * 유지보수 포인트: 예외 처리, 로깅, 멱등성 처리를 여기에 집중적으로 추가합니다.
 */
public class PipelineOrchestrator {
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

    /**
     * 오케스트레이터가 사용할 단계 구현체를 주입합니다.
     * 목적: 교체 가능한 구조로 만들어 테스트와 확장을 쉽게 합니다.
     */
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

    /**
     * 원본 데이터를 표준 파이프라인으로 처리합니다.
     * 목적: 원본 저장 -> 정규화 -> 분류 -> 검증 -> 저장/격리 순서를 보장합니다.
     * 기능: 검증 통과 시 표준 데이터 저장, 실패 시 격리로 분기합니다.
     * 이유: 원본은 항상 보관해야 하며, 실패 사유는 추적 가능해야 합니다.
     */
    public void process(RawEnvelope rawEnvelope) {
        // 원본 데이터는 반드시 보관해야 하므로 가장 먼저 저장합니다.
        storeService.storeRaw(rawEnvelope);

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
}
