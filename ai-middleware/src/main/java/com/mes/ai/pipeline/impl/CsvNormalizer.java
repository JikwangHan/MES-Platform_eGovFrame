package com.mes.ai.pipeline.impl;

import com.mes.ai.model.EnvelopeCandidate;
import com.mes.ai.model.MessageType;
import com.mes.ai.model.RawEnvelope;
import com.mes.ai.pipeline.Normalizer;
import com.mes.ai.util.Base64Utils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CSV 포맷을 기준으로 정규화합니다.
 * 목적: JSON 외에도 기본적인 CSV 수신을 최소 수준으로 처리합니다.
 * 기능: 헤더+데이터 2줄 구조 또는 key=value 목록을 Map으로 변환합니다.
 * 이유: 비정형 데이터가 들어오더라도 최소 파싱 경로를 확보하기 위함입니다.
 * 유지보수: 복잡한 CSV 규칙이 필요하면 파서를 교체합니다.
 */
public class CsvNormalizer implements Normalizer {
    /**
     * 목적: CSV payload를 표준 Map 구조로 변환합니다.
     * 기능: Base64 복원 후 CSV 파싱과 messageType 추출을 수행합니다.
     * 이유: 분류/검증 단계가 동일한 구조를 요구하기 때문입니다.
     * 유지보수: CSV 파싱 규칙 변경 시 이 메서드를 수정합니다.
     */
    @Override
    public EnvelopeCandidate normalize(RawEnvelope rawEnvelope) {
        // 후보 객체는 항상 생성해 반환하여 다음 단계 계약을 지킵니다.
        EnvelopeCandidate candidate = new EnvelopeCandidate();
        candidate.setRawEnvelope(rawEnvelope);

        if (rawEnvelope == null || rawEnvelope.getPayloadBase64() == null) {
            // 원본이 없으면 빈 payload로 넘겨 검증 단계에서 실패 처리합니다.
            candidate.setNormalizedPayload(Collections.emptyMap());
            return candidate;
        }

        // Base64로 보관된 원본을 복원합니다.
        String payload = Base64Utils.decodeToString(rawEnvelope.getPayloadBase64());
        Map<String, Object> parsed = parseCsv(payload);
        candidate.setNormalizedPayload(parsed);
        // messageType은 표준 메시지 분류에 필요하므로 여기서 추출합니다.
        candidate.setMessageType(extractMessageType(parsed));
        return candidate;
    }

    /**
     * CSV 문자열을 Map 형태로 변환합니다.
     * 목적: CSV를 key-value 구조로 변환해 후속 검증에 사용합니다.
     * 기능: 헤더/데이터 2줄 또는 key=value 형식 입력을 처리합니다.
     * 이유: 최소 규칙으로도 정규화 가능해야 Unknown Ingest를 줄일 수 있습니다.
     * 유지보수: 구분자 변경/컬럼 확장 시 이 메서드를 수정합니다.
     */
    private Map<String, Object> parseCsv(String payload) {
        if (payload == null || payload.trim().isEmpty()) {
            return Collections.emptyMap();
        }

        String[] lines = payload.split("\\r?\\n");
        if (lines.length == 0) {
            return Collections.emptyMap();
        }

        // 첫 줄이 key=value 형태라면 이를 우선 처리합니다.
        if (lines[0].contains("=")) {
            return parseKeyValueLine(lines[0]);
        }

        // 기본 규칙: 1행 헤더, 2행 데이터
        if (lines.length < 2) {
            return Collections.emptyMap();
        }

        String[] headers = splitCsvLine(lines[0]);
        String[] values = splitCsvLine(lines[1]);

        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < headers.length; i++) {
            String key = normalizeCell(headers[i]);
            if (key.isEmpty()) {
                continue;
            }
            String value = i < values.length ? normalizeCell(values[i]) : "";
            result.put(key, value);
        }
        return result;
    }

    /**
     * key=value 목록을 Map으로 변환합니다.
     * 목적: 장비에서 단일 라인으로 전송되는 간단 포맷을 처리합니다.
     * 기능: 콤마로 분리된 key=value 쌍을 Map에 저장합니다.
     * 이유: 헤더/데이터 분리 없는 입력도 최소 수준으로 수용하기 위함입니다.
     * 유지보수: 다른 구분자 지원이 필요하면 여기에서 확장합니다.
     */
    private Map<String, Object> parseKeyValueLine(String line) {
        String[] pairs = line.split(",");
        Map<String, Object> result = new LinkedHashMap<>();
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            if (parts.length != 2) {
                continue;
            }
            String key = normalizeCell(parts[0]);
            String value = normalizeCell(parts[1]);
            if (!key.isEmpty()) {
                result.put(key, value);
            }
        }
        return result;
    }

    /**
     * CSV 한 줄을 쉼표로 분리합니다.
     * 목적: 최소 구현을 위해 단순 분리 로직을 사용합니다.
     * 기능: 쉼표 기준으로 컬럼을 분리해 반환합니다.
     * 이유: 복잡한 CSV 파서는 추후 확장 단계에서 도입합니다.
     * 유지보수: 따옴표/이스케이프 지원이 필요하면 교체합니다.
     */
    private String[] splitCsvLine(String line) {
        if (line == null) {
            return new String[0];
        }
        return line.split(",", -1);
    }

    /**
     * 셀 문자열을 정리합니다.
     * 목적: 공백 제거와 간단한 따옴표 제거를 수행합니다.
     * 기능: 양끝 공백과 감싸는 따옴표를 제거합니다.
     * 이유: 후속 검증 단계의 값 비교를 단순화하기 위함입니다.
     * 유지보수: 복잡한 인코딩 처리 필요 시 이 메서드를 확장합니다.
     */
    private String normalizeCell(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() >= 2) {
            char first = trimmed.charAt(0);
            char last = trimmed.charAt(trimmed.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return trimmed.substring(1, trimmed.length() - 1).trim();
            }
        }
        return trimmed;
    }

    /**
     * 메시지 유형을 다양한 키 후보에서 찾아 MessageType으로 변환합니다.
     * 목적: 입력 포맷의 키 차이를 흡수하여 분류 일관성을 높입니다.
     * 기능: 후보 키를 순차 조회해 MessageType으로 변환합니다.
     * 이유: 입력 포맷이 서로 달라도 동일한 분류 결과를 얻기 위함입니다.
     * 유지보수: 새 키 후보가 추가되면 이 메서드를 확장합니다.
     */
    private MessageType extractMessageType(Map<String, Object> payload) {
        if (payload == null) {
            return null;
        }
        Object value = payload.get("messageType");
        if (value == null) {
            value = payload.get("message_type");
        }
        if (value == null) {
            value = payload.get("type");
        }
        if (value == null) {
            return null;
        }
        String raw = String.valueOf(value).trim();
        if (raw.isEmpty()) {
            return null;
        }
        try {
            return MessageType.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
