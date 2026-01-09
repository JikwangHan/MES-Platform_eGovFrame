package com.mes.ai.service.impl;

import com.mes.ai.model.ScanRequest;
import com.mes.ai.model.ScanResult;
import com.mes.ai.model.ScanStatus;
import com.mes.ai.service.SecurityScanService;
import com.mes.ai.util.Base64Utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * ClamAV 기반 보안 스캔 구현체입니다.
 * 목적: 수신 데이터의 악성코드를 탐지해 파이프라인을 보호합니다.
 * 기능: clamscan 명령을 호출해 파일을 검사하고 결과를 반환합니다.
 * 이유: 온프레미스 환경에서 가볍게 적용할 수 있는 스캔 엔진을 제공하기 위함입니다.
 * 유지보수: 실행 명령/옵션 변경 시 이 클래스에서 조정합니다.
 */
public class ClamAvSecurityScanService implements SecurityScanService {
    /** 기본 스캔 명령입니다. */
    private static final String DEFAULT_COMMAND = "clamscan";
    /** 기본 타임아웃(밀리초)입니다. */
    private static final long DEFAULT_TIMEOUT_MS = 10000L;

    /**
     * 목적: ClamAV를 이용해 스캔을 수행합니다.
     * 기능: payload를 파일로 저장 후 clamscan을 실행하고 결과를 반환합니다.
     * 이유: clamscan은 파일 경로 기반으로 동작하기 때문입니다.
     * 유지보수: 스캔 옵션/타임아웃 정책 변경 시 이 메서드를 수정합니다.
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

        Path tempFile = null;
        String targetPath = normalizePath(request.getPayloadPath());
        try {
            if (targetPath == null || targetPath.isEmpty()) {
                byte[] payload = Base64Utils.decodeToBytes(request.getPayloadBase64());
                if (payload == null || payload.length == 0) {
                    result.setStatus(ScanStatus.ERROR);
                    result.setError("스캔할 페이로드가 없습니다.");
                    return finish(result, start);
                }
                // 목적: 임시 파일로 저장해 clamscan이 검사할 수 있게 합니다.
                // 이유: clamscan은 파일 경로를 입력으로 받기 때문입니다.
                tempFile = Files.createTempFile("ai-middleware-scan-", ".bin");
                Files.write(tempFile, payload);
                targetPath = tempFile.toString();
            }

            String command = System.getProperty("ai.security.scan.command", DEFAULT_COMMAND);
            long timeoutMs = parseLong(System.getProperty("ai.security.scan.timeoutMs"), DEFAULT_TIMEOUT_MS);

            ProcessBuilder builder = new ProcessBuilder(
                    command,
                    "--no-summary",
                    targetPath
            );
            builder.redirectErrorStream(true);
            Process process = builder.start();

            boolean finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                result.setStatus(ScanStatus.TIMEOUT);
                result.setError("스캔 시간 초과");
                return finish(result, start);
            }

            String output = readOutput(process);
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                result.setStatus(ScanStatus.CLEAN);
            } else if (exitCode == 1) {
                result.setStatus(ScanStatus.INFECTED);
                result.setSignature(extractSignature(output));
            } else {
                result.setStatus(ScanStatus.ERROR);
                result.setError("스캔 실패: " + output);
            }
            result.setEngine("CLAMAV");
            return finish(result, start);
        } catch (Exception ex) {
            result.setStatus(ScanStatus.ERROR);
            result.setError("스캔 실행 오류: " + ex.getMessage());
            result.setEngine("CLAMAV");
            return finish(result, start);
        } finally {
            // 목적: 임시 파일을 정리합니다.
            // 이유: 테스트 반복 시 디스크 누적을 방지하기 위함입니다.
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception ignore) {
                    // 삭제 실패는 스캔 결과에 영향을 주지 않으므로 무시합니다.
                }
            }
        }
    }

    /**
     * 목적: 스캔 결과에 공통 정보를 기록합니다.
     * 기능: 소요 시간을 계산해 결과에 저장합니다.
     * 이유: 운영 지표를 일관되게 관리하기 위함입니다.
     * 유지보수: 추가 메트릭 저장 시 이 메서드를 확장합니다.
     */
    private ScanResult finish(ScanResult result, long start) {
        result.setDurationMs(System.currentTimeMillis() - start);
        return result;
    }

    /**
     * 목적: clamscan 출력에서 시그니처를 추출합니다.
     * 기능: " FOUND" 구문을 기반으로 시그니처 문자열을 파싱합니다.
     * 이유: 감염 근거를 기록하기 위함입니다.
     * 유지보수: 출력 포맷 변경 시 파싱 규칙을 수정합니다.
     */
    private String extractSignature(String output) {
        if (output == null) {
            return null;
        }
        String marker = " FOUND";
        int found = output.indexOf(marker);
        if (found <= 0) {
            return null;
        }
        int colon = output.lastIndexOf(": ", found);
        if (colon < 0) {
            return null;
        }
        return output.substring(colon + 2, found).trim();
    }

    /**
     * 목적: 프로세스 출력을 안전하게 읽습니다.
     * 기능: 표준 출력 내용을 문자열로 반환합니다.
     * 이유: 오류 메시지를 결과에 담아 운영 분석에 활용합니다.
     * 유지보수: 인코딩/스트림 처리 변경 시 이 메서드를 수정합니다.
     */
    private String readOutput(Process process) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
        )) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
            return builder.toString().trim();
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * 목적: 숫자 문자열을 안전하게 파싱합니다.
     * 기능: 파싱 실패 시 기본값을 반환합니다.
     * 이유: 잘못된 설정 값이 있어도 기본값으로 복구하기 위함입니다.
     * 유지보수: 숫자 범위 정책 변경 시 이 메서드를 수정합니다.
     */
    private long parseLong(String value, long fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    /**
     * 목적: 경로 입력을 정리합니다.
     * 기능: null/공백을 정리해 유효한 경로만 반환합니다.
     * 이유: 공백/널 처리로 예외를 줄이기 위함입니다.
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
