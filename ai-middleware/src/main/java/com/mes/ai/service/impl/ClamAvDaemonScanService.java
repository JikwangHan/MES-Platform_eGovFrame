package com.mes.ai.service.impl;

import com.mes.ai.model.ScanRequest;
import com.mes.ai.model.ScanResult;
import com.mes.ai.model.ScanStatus;
import com.mes.ai.service.SecurityScanService;
import com.mes.ai.util.Base64Utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * ClamAV daemon(clamd) 기반 보안 스캔 구현체입니다.
 * 목적: 수신 데이터의 악성코드를 빠르게 검사하고 파이프라인을 보호합니다.
 * 기능: clamd의 INSTREAM 프로토콜로 바이트 스트림을 전송해 스캔합니다.
 * 이유: 컨테이너/서비스 분리 환경에서도 가볍게 실시간 검사하기 위함입니다.
 * 유지보수: 호스트/포트/타임아웃 정책 변경 시 이 클래스에서 조정합니다.
 */
public class ClamAvDaemonScanService implements SecurityScanService {
    /** 기본 clamd 호스트입니다. */
    private static final String DEFAULT_HOST = "127.0.0.1";
    /** 기본 clamd 포트입니다. */
    private static final int DEFAULT_PORT = 3310;
    /** 기본 타임아웃(밀리초)입니다. */
    private static final int DEFAULT_TIMEOUT_MS = 10000;

    /**
     * 목적: clamd를 이용해 스캔을 수행합니다.
     * 기능: payload를 준비해 clamd로 전송하고 결과를 해석합니다.
     * 이유: 실시간 스캔으로 파이프라인을 보호하기 위함입니다.
     * 유지보수: 스캔 옵션/응답 처리 규칙 변경 시 이 메서드를 수정합니다.
     */
    @Override
    public ScanResult scan(ScanRequest request) {
        ScanResult result = new ScanResult();
        long start = System.currentTimeMillis();

        if (request == null) {
            result.setStatus(ScanStatus.ERROR);
            result.setError("스캔 요청이 없습니다.");
            return finish(result, start);
        }

        try {
            byte[] payload = resolvePayload(request);
            if (payload == null || payload.length == 0) {
                result.setStatus(ScanStatus.ERROR);
                result.setError("스캔할 페이로드가 없습니다.");
                return finish(result, start);
            }

            String host = System.getProperty("ai.security.scan.clamdHost", DEFAULT_HOST);
            int port = parseInt(System.getProperty("ai.security.scan.clamdPort"), DEFAULT_PORT);
            int timeoutMs = parseInt(System.getProperty("ai.security.scan.timeoutMs"), DEFAULT_TIMEOUT_MS);

            String response = scanByDaemon(host, port, timeoutMs, payload);
            applyResponse(result, response);
            result.setEngine("CLAMAV_DAEMON");
            return finish(result, start);
        } catch (Exception ex) {
            result.setStatus(ScanStatus.ERROR);
            result.setError("스캔 실행 오류: " + ex.getMessage());
            result.setEngine("CLAMAV_DAEMON");
            return finish(result, start);
        }
    }

    /**
     * 목적: clamd에 INSTREAM 프로토콜로 데이터를 전송합니다.
     * 기능: 요청 바이트를 청크로 나눠 전송하고 결과 문자열을 받습니다.
     * 이유: clamd는 파일 경로 대신 스트림 스캔을 권장하기 때문입니다.
     * 유지보수: 청크 크기/프로토콜 변경 시 이 메서드를 수정합니다.
     */
    private String scanByDaemon(String host, int port, int timeoutMs, byte[] payload) throws Exception {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            socket.setSoTimeout(timeoutMs);

            // 목적: 출력 스트림을 유지한 채 요청을 전송합니다.
            // 이유: 스트림을 닫으면 소켓이 종료되어 응답을 못 받을 수 있습니다.
            OutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
            // 목적: INSTREAM 모드 시작을 알립니다.
            // 이유: clamd TCP는 "zINSTREAM\0" 명령을 안정적으로 처리합니다.
            outputStream.write("zINSTREAM\0".getBytes(StandardCharsets.US_ASCII));

            // 목적: 페이로드를 길이+데이터 형태로 전송합니다.
            // 이유: clamd INSTREAM 규격이 4바이트 길이 헤더를 요구합니다.
            int offset = 0;
            int chunkSize = 8192;
            while (offset < payload.length) {
                int len = Math.min(chunkSize, payload.length - offset);
                writeLength(outputStream, len);
                outputStream.write(payload, offset, len);
                offset += len;
            }
            // 목적: 전송 종료를 알립니다.
            // 이유: 길이 0 청크가 종료 신호입니다.
            writeLength(outputStream, 0);
            outputStream.flush();
            // 목적: 출력만 종료하고 연결은 유지합니다.
            // 이유: 응답을 읽기 위해 소켓 입력을 남겨야 합니다.
            socket.shutdownOutput();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new BufferedInputStream(socket.getInputStream()), StandardCharsets.UTF_8)
            )) {
                return reader.readLine();
            }
        }
    }

    /**
     * 목적: clamd 응답 문자열을 결과 객체에 반영합니다.
     * 이유: FOUND/OK/ERROR 구분에 따라 상태를 결정하기 위함입니다.
     * 기능: 응답 문자열을 해석해 ScanStatus/Signature를 설정합니다.
     * 유지보수: 응답 포맷이 변경되면 이 메서드를 수정합니다.
     */
    private void applyResponse(ScanResult result, String response) {
        if (response == null || response.trim().isEmpty()) {
            result.setStatus(ScanStatus.ERROR);
            result.setError("clamd 응답이 비어 있습니다.");
            return;
        }
        String normalized = response.trim();
        if (normalized.endsWith("OK")) {
            result.setStatus(ScanStatus.CLEAN);
            return;
        }
        if (normalized.contains("FOUND")) {
            result.setStatus(ScanStatus.INFECTED);
            result.setSignature(extractSignature(normalized));
            return;
        }
        result.setStatus(ScanStatus.ERROR);
        result.setError("clamd 응답 오류: " + normalized);
    }

    /**
     * 목적: clamd 응답에서 시그니처를 추출합니다.
     * 이유: 감염 근거를 기록해 추적 가능하도록 하기 위함입니다.
     * 기능: "FOUND" 구문을 기준으로 시그니처 문자열을 파싱합니다.
     * 유지보수: 응답 포맷 변경 시 파싱 규칙을 수정합니다.
     */
    private String extractSignature(String response) {
        int found = response.indexOf("FOUND");
        if (found <= 0) {
            return null;
        }
        int colon = response.lastIndexOf(": ", found);
        if (colon < 0) {
            return null;
        }
        return response.substring(colon + 2, found).trim();
    }

    /**
     * 목적: 스캔할 바이트를 결정합니다.
     * 이유: 경로/베이스64 입력 모두를 지원하기 위함입니다.
     * 기능: 경로가 있으면 파일을 읽고, 없으면 Base64를 디코딩합니다.
     * 유지보수: 입력 방식이 추가되면 이 메서드를 확장합니다.
     */
    private byte[] resolvePayload(ScanRequest request) throws Exception {
        String path = normalizePath(request.getPayloadPath());
        if (path != null) {
            return Files.readAllBytes(Path.of(path));
        }
        return Base64Utils.decodeToBytes(request.getPayloadBase64());
    }

    /**
     * 목적: 4바이트 길이 헤더를 빅엔디안으로 기록합니다.
     * 이유: clamd INSTREAM 규격을 맞추기 위함입니다.
     * 기능: 길이를 4바이트로 분해해 출력 스트림에 기록합니다.
     * 유지보수: 프로토콜 변경 시 이 메서드를 수정합니다.
     */
    private void writeLength(OutputStream outputStream, int length) throws Exception {
        outputStream.write((length >> 24) & 0xFF);
        outputStream.write((length >> 16) & 0xFF);
        outputStream.write((length >> 8) & 0xFF);
        outputStream.write(length & 0xFF);
    }

    /**
     * 목적: 결과에 공통 정보(소요 시간)를 기록합니다.
     * 이유: 운영 지표를 일관되게 남기기 위함입니다.
     * 기능: 시작 시각을 기준으로 소요 시간을 계산합니다.
     * 유지보수: 추가 메트릭 저장 시 이 메서드를 확장합니다.
     */
    private ScanResult finish(ScanResult result, long start) {
        result.setDurationMs(System.currentTimeMillis() - start);
        return result;
    }

    /**
     * 목적: 숫자 문자열을 안전하게 파싱합니다.
     * 이유: 잘못된 설정 값에도 기본값으로 복구하기 위함입니다.
     * 기능: 파싱 실패 시 기본값을 반환합니다.
     * 유지보수: 범위 검증 규칙 변경 시 이 메서드를 수정합니다.
     */
    private int parseInt(String value, int fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    /**
     * 목적: 경로 입력을 정리합니다.
     * 이유: 공백/널 처리로 예외를 줄이기 위함입니다.
     * 기능: null/공백을 제거해 유효 경로만 반환합니다.
     * 유지보수: 경로 규칙 변경 시 이 메서드를 수정합니다.
     */
    private String normalizePath(String path) {
        if (path == null) {
            return null;
        }
        String trimmed = path.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
