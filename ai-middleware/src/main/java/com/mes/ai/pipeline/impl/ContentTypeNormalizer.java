package com.mes.ai.pipeline.impl;

import com.mes.ai.model.EnvelopeCandidate;
import com.mes.ai.model.RawEnvelope;
import com.mes.ai.pipeline.Normalizer;

/**
 * 콘텐츠 타입에 따라 정규화 구현체를 선택합니다.
 * 목적: JSON/CSV 등 포맷별 처리 로직을 한 곳에서 분기합니다.
 * 기능: contentType 힌트를 읽고 적절한 Normalizer로 위임합니다.
 * 이유: 신규 포맷 추가 시 파이프라인 변경을 최소화합니다.
 * 유지보수: 새 포맷 도입 시 이 클래스에 분기만 추가합니다.
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
     * 기능: JSON/CSV/TSV 기본 정규화기를 생성합니다.
     * 이유: 외부 DI 없이도 바로 사용할 수 있게 하기 위함입니다.
     * 유지보수: 기본 구현체 변경 시 이 생성자를 수정합니다.
     */
    public ContentTypeNormalizer() {
        this(new JsonNormalizer(), new CsvNormalizer(), new TsvNormalizer());
    }

    /**
     * 목적: 포맷별 구현체를 주입받습니다.
     * 기능: 외부에서 주입한 정규화기를 내부 필드에 저장합니다.
     * 이유: 테스트나 확장 시 교체가 쉽도록 합니다.
     * 유지보수: 새 포맷 정규화기를 추가할 때 생성자를 확장합니다.
     */
    public ContentTypeNormalizer(Normalizer jsonNormalizer, Normalizer csvNormalizer, Normalizer tsvNormalizer) {
        this.jsonNormalizer = jsonNormalizer;
        this.csvNormalizer = csvNormalizer;
        this.tsvNormalizer = tsvNormalizer;
    }

    /**
     * 목적: 콘텐츠 타입에 맞는 정규화기를 선택합니다.
     * 기능: TSV/CSV 여부를 판별한 뒤 해당 구현체로 위임합니다.
     * 이유: 포맷별 파싱 방식이 다르기 때문입니다.
     * 유지보수: 포맷 판별 규칙 변경 시 이 메서드를 수정합니다.
     */
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
     * 기능: contentType 문자열에 csv 힌트가 포함되는지 확인합니다.
     * 이유: 간단한 규칙으로 빠르게 포맷을 분기하기 위함입니다.
     * 유지보수: 힌트 규칙이 늘어나면 여기에서 확장합니다.
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
     * 기능: contentType 문자열에 tsv 힌트가 포함되는지 확인합니다.
     * 이유: TSV는 CSV와 파싱 규칙이 다르기 때문입니다.
     * 유지보수: 추가 MIME 타입이 생기면 이 메서드를 확장합니다.
     */
    private boolean isTsv(RawEnvelope rawEnvelope) {
        if (rawEnvelope == null || rawEnvelope.getContentType() == null) {
            return false;
        }
        String type = rawEnvelope.getContentType().toLowerCase();
        return type.contains("tsv") || type.contains("text/tab-separated-values");
    }
}
