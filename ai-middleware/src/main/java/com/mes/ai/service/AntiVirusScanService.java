package com.mes.ai.service;

import com.mes.ai.model.AntiVirusScanResult;
import com.mes.ai.model.InboundObject;

/**
 * 안티바이러스 스캔 전용 서비스 인터페이스입니다.
 * 목적: 스캔 로직을 미들웨어에서 분리해 독립적으로 운영합니다.
 * 기능: 인바운드 객체를 받아 판정 결과를 반환합니다.
 * 이유: 큐/워커 기반 비동기 구조를 안정적으로 연결하기 위함입니다.
 */
public interface AntiVirusScanService {
    /**
     * 인바운드 객체를 스캔합니다.
     * 목적: 감염 여부와 오류를 판정해 후속 분기 기준을 제공합니다.
     * 이유: OK/FOUND/ERROR 기준을 단일화하기 위함입니다.
     */
    AntiVirusScanResult scan(InboundObject inboundObject);
}
