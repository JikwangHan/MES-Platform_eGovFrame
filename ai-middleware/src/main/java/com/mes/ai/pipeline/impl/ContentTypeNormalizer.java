package com.mes.ai.pipeline.impl;

import com.mes.ai.model.EnvelopeCandidate;
import com.mes.ai.model.RawEnvelope;
import com.mes.ai.pipeline.Normalizer;

/**
 * 콘텐츠 타입에 따라 정규화 구현체를 선택합니다.
 * 목적: JSON/CSV 등 포맷별 처리 로직을 한 곳에서 분기합니다.
 * 기능: contentType 힌트를 읽고 적절한 Normalizer로 위임합니다.
 * 이유: 신규 포맷 추가 시 파이프라인 변경을 최소화합니다.
 */
public class ContentTypeNormalizer implements Normalizer {
    /** JSON 처리 구현체입니다. */
    private final Normalizer jsonNormalizer;
    /** CSV 처리 구현체입니다. */
    private final Normalizer csvNormalizer;
    /** TSV 처리 구현체입니다. */
    private final Normalizer tsvNormalizer;

    /**
     * 목적: 기본 구현체를 구성합니다.
     * 이유: 외부 DI 없이도 바로 사용할 수 있게 합니다.
     */
    public ContentTypeNormalizer() {
        this(new JsonNormalizer(), new CsvNormalizer(), new TsvNormalizer());
    }

    /**
     * 목적: 포맷별 구현체를 주입받습니다.
     * 이유: 테스트나 확장 시 교체가 쉽도록 합니다.
     */
    public ContentTypeNormalizer(Normalizer jsonNormalizer, Normalizer csvNormalizer, Normalizer tsvNormalizer) {
        this.jsonNormalizer = jsonNormalizer;
        this.csvNormalizer = csvNormalizer;
        this.tsvNormalizer = tsvNormalizer;
    }

    @Override
    public EnvelopeCandidate normalize(RawEnvelope rawEnvelope) {
        if (isTsv(rawEnvelope)) {
            return tsvNormalizer.normalize(rawEnvelope);
        }
        if (isCsv(rawEnvelope)) {
            return csvNormalizer.normalize(rawEnvelope);
        }
        // 기본값은 JSON으로 처리합니다.
        return jsonNormalizer.normalize(rawEnvelope);
    }

    /**
     * 콘텐츠 타입이 CSV인지 판단합니다.
     * 목적: 최소 규칙으로 포맷 분기를 수행합니다.
     */
    private boolean isCsv(RawEnvelope rawEnvelope) {
        if (rawEnvelope == null || rawEnvelope.getContentType() == null) {
            return false;
        }
        String type = rawEnvelope.getContentType().toLowerCase();
        return type.contains("csv") || type.contains("text/csv");
    }

    /**
     * 콘텐츠 타입이 TSV인지 판단합니다.
     * 목적: 탭 구분 포맷을 식별하기 위함입니다.
     */
    private boolean isTsv(RawEnvelope rawEnvelope) {
        if (rawEnvelope == null || rawEnvelope.getContentType() == null) {
            return false;
        }
        String type = rawEnvelope.getContentType().toLowerCase();
        return type.contains("tsv") || type.contains("text/tab-separated-values");
    }
}
