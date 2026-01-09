package com.mes.ai.util;

import java.time.Instant;

/**
 * UTC 기준 시각을 생성합니다.
 * 목적: 모든 기록 시간을 UTC로 통일하여 시간대 혼선을 방지합니다.
 * 기능: ISO-8601 문자열 형태의 현재 시각을 제공합니다.
 * 이유: 로그/데이터 분석 시 표준 시간 기준이 필요합니다.
 * 유지보수: 구조 변경 시 이 클래스에서 조정합니다.
 */
public final class TimeUtils {
    /**
     * 목적: 유틸리티 클래스의 인스턴스화를 방지합니다.
     * 기능: 외부에서 생성자를 호출할 수 없게 합니다.
     * 이유: 모든 기능을 정적 메서드로 제공하기 위함입니다.
     */
    private TimeUtils() {
    }

    /**
     * 현재 시각을 UTC ISO-8601 문자열로 반환합니다.
     * 목적: 저장/로그에 일관된 형식의 시각을 기록합니다.
     * 기능: 필요한 처리를 수행합니다.
     * 이유: 기능 흐름을 한 곳에서 담당하기 위함입니다.
     * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
     */
    public static String nowIsoUtc() {
        return Instant.now().toString();
    }
}
