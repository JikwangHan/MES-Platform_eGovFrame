package com.mes.ai.service.impl;

import com.mes.ai.model.AntiVirusScanResult;
import com.mes.ai.model.AntiVirusVerdict;
import com.mes.ai.model.ScanQueueItem;
import com.mes.ai.model.UnknownIngestRecord;
import com.mes.ai.service.PostScanHandler;
import com.mes.ai.service.PipelineOrchestrator;
import com.mes.ai.service.UnknownIngestService;

/**
 * 스캔 결과를 파이프라인 분기로 연결하는 기본 핸들러입니다.
 * 목적: OK는 정상 파이프라인, FOUND/ERROR는 격리로 분기합니다.
 * 기능: 스캔 결과를 UnknownIngest로 기록하고, 정상 케이스만 처리합니다.
 * 이유: 스캔 전용 서비스 구조를 실제 파이프라인과 연결하기 위함입니다.
 */
public class PostScanPipelineHandler implements PostScanHandler {
    private final PipelineOrchestrator orchestrator;
    private final UnknownIngestService unknownIngestService;

    /**
     * 목적: 파이프라인과 격리 서비스를 주입받습니다.
     * 이유: 스캔 결과에 따라 처리 경로를 분리하기 위함입니다.
     */
    public PostScanPipelineHandler(
            PipelineOrchestrator orchestrator,
            UnknownIngestService unknownIngestService
    ) {
        this.orchestrator = orchestrator;
        this.unknownIngestService = unknownIngestService;
    }

    @Override
    public void handle(ScanQueueItem item, AntiVirusScanResult result) {
        if (item == null || item.getRawEnvelope() == null) {
            return;
        }
        if (result == null || result.getVerdict() == null) {
            unknownIngestService.save(buildUnknownRecord(item, result, "AV_SCAN_ERROR"));
            return;
        }
        if (result.getVerdict() == AntiVirusVerdict.OK) {
            // 스캔 통과 시에만 파이프라인을 진행합니다.
            orchestrator.processAfterScan(item.getRawEnvelope());
            return;
        }
        String reason = result.getVerdict() == AntiVirusVerdict.FOUND ? "AV_SCAN_FOUND" : "AV_SCAN_ERROR";
        unknownIngestService.save(buildUnknownRecord(item, result, reason));
    }

    /**
     * 목적: 스캔 실패/감염 시 UnknownIngest 레코드를 생성합니다.
     * 이유: 감사 로그와 재처리를 위해 원본과 스캔 정보를 보관합니다.
     */
    private UnknownIngestRecord buildUnknownRecord(ScanQueueItem item, AntiVirusScanResult result, String reason) {
        UnknownIngestRecord record = new UnknownIngestRecord();
        if (item != null && item.getRawEnvelope() != null) {
            record.setReceivedAt(item.getRawEnvelope().getReceivedAt());
            record.setIngressType(item.getRawEnvelope().getIngressType());
            record.setPayloadBase64(item.getRawEnvelope().getPayloadBase64());
            record.setPayloadHash(item.getRawEnvelope().getPayloadHash());
            record.setSourceIdHash(item.getRawEnvelope().getSourceIdHash());
            record.setContentType(item.getRawEnvelope().getContentType());
        }
        if (result != null) {
            if (result.getVerdict() != null) {
                record.setScanStatus(result.getVerdict().name());
            }
            record.setScanEngine(result.getEngine());
            record.setScanSignature(result.getThreatName());
            record.setScanDurationMs(result.getDurationMs());
        }
        record.setQuarantineReason(reason);
        return record;
    }
}
