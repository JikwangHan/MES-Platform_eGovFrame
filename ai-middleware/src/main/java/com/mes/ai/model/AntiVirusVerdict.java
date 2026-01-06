package com.mes.ai.model;

/**
 * 안티바이러스 스캔 판정 상태입니다.
 * 목적: 스캔 결과 분기를 명확히 구분합니다.
 * 기능: OK/FOUND/ERROR 세 가지 상태를 정의합니다.
 * 이유: 파이프라인에서 격리/정상 처리 기준을 일관되게 적용하기 위함입니다.
 */
public enum AntiVirusVerdict {
    OK,
    FOUND,
    ERROR
}
