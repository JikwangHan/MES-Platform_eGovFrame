package com.mes.ai.util;

import java.time.Instant;

/**
 * UTC 기준 시각을 생성합니다.
 * 목적: 모든 기록 시간을 UTC로 통일하여 시간대 혼선을 방지합니다.
 * 기능: ISO-8601 문자열 형태의 현재 시각을 제공합니다.
 * 이유: 로그/데이터 분석 시 표준 시간 기준이 필요합니다.
 */
public final class TimeUtils {
    /** 유틸리티 클래스이므로 외부에서 인스턴스화하지 못하게 합니다. */
    private TimeUtils() {
    }

    /**
     * 현재 시각을 UTC ISO-8601 문자열로 반환합니다.
     * 목적: 저장/로그에 일관된 형식의 시각을 기록합니다.
     */
    public static String nowIsoUtc() {
        return Instant.now().toString();
    }
}
