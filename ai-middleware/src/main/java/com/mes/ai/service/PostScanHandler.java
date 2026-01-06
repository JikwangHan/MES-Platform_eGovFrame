package com.mes.ai.service;

import com.mes.ai.model.AntiVirusScanResult;
import com.mes.ai.model.ScanQueueItem;

/**
 * 스캔 완료 후처리 핸들러입니다.
 * 목적: 스캔 결과에 따라 후속 파이프라인을 분기합니다.
 * 기능: OK/FOUND/ERROR 처리 로직을 한 곳에서 정의합니다.
 * 이유: 워커는 스캔만 수행하고, 분기 정책은 분리하기 위함입니다.
 */
public interface PostScanHandler {
    /**
     * 스캔 결과를 받아 후속 처리를 수행합니다.
     * 목적: OK면 정상 파이프라인, 실패면 격리로 이동합니다.
     */
    void handle(ScanQueueItem item, AntiVirusScanResult result);
}
