package com.mes.ai.model;

/**
 * 보안 스캔 결과 상태를 정의합니다.
 * 목적: 스캔 결과를 표준 값으로 관리해 후속 처리 분기를 단순화합니다.
 * 기능: 역할에 맞는 핵심 동작을 제공합니다.
 * 이유: 문자열 하드코딩을 줄여 일관된 정책 적용을 보장합니다.
 * 유지보수: 확장/변경 시 이 클래스에서 정책을 조정합니다.
 */
public enum ScanStatus {
    CLEAN,
    INFECTED,
    ERROR,
    TIMEOUT
}
