package com.mes.web.common.validation;

import java.util.Map;

/**
 * 목적: 조회 조건을 공통 처리한다.
 * 기능: 페이징/정렬을 위한 기본 파라미터를 보정한다.
 * 이유: 목록 조회 시 일관된 기준을 적용하기 위함이다.
 * 유지보수: 조회 정책 변경 시 이 클래스만 수정한다.
 */
public final class CriteriaUtils {

    private CriteriaUtils() {
        // 유틸리티 클래스이므로 인스턴스 생성을 막는다.
    }

    /**
     * 목적: 페이징 파라미터를 보정한다.
     * 기능: page/pageSize 값을 기본값으로 채우고 offset/limit을 생성한다.
     * 이유: 목록 조회에서 과다 조회를 방지하고 성능을 확보하기 위함이다.
     * 유지보수: 기본/최대 값 변경 시 이 메서드를 수정한다.
     */
    public static void applyPaging(Map<String, Object> criteria, int defaultSize, int maxSize) {
        int page = parsePositiveInt(criteria.get("page"), 1);
        int pageSize = parsePositiveInt(criteria.get("pageSize"), defaultSize);
        if (pageSize > maxSize) {
            pageSize = maxSize;
        }
        int offset = (page - 1) * pageSize;
        criteria.put("page", page);
        criteria.put("pageSize", pageSize);
        criteria.put("offset", offset);
        criteria.put("limit", pageSize);
    }

    /**
     * 목적: 양의 정수를 파싱한다.
     * 기능: 입력값이 없거나 잘못된 경우 기본값을 반환한다.
     * 이유: 파라미터 오류로 인한 예외를 방지하기 위함이다.
     * 유지보수: 기본값 정책 변경 시 호출부를 조정한다.
     */
    private static int parsePositiveInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(value.toString());
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
