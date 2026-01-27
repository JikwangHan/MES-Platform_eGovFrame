package com.mes.web.common.mail;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * 목적: 이메일 재시도 대기열을 관리한다.
 * 기능: 실패 메일을 큐에 적재하고 재시도 대상 목록을 제공한다.
 * 이유: 발송 실패를 누락하지 않고 복구하기 위함이다.
 * 유지보수: 재시도 정책이 바뀌면 큐 동작을 수정한다.
 */
@Service
public class EmailRetryQueue {

    private final Deque<EmailRetryItem> queue = new ArrayDeque<EmailRetryItem>();

    /**
     * 목적: 큐 사용 여부를 판단한다.
     * 기능: 시스템 속성/환경변수 값을 확인한다.
     * 이유: 운영 정책에 따라 큐를 끌 수 있어야 하기 때문이다.
     * 유지보수: 설정 키 변경 시 로직을 보완한다.
     */
    public boolean isEnabled() {
        String value = read("mes.mail.retry.queue.enabled", "MES_MAIL_RETRY_QUEUE_ENABLED");
        if (value == null) {
            return true;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * 목적: 큐 최대 적재 건수를 조회한다.
     * 기능: 설정값을 읽어 최대 크기를 반환한다.
     * 이유: 메모리 과다 사용을 방지하기 위함이다.
     * 유지보수: 정책 변경 시 기본값을 수정한다.
     */
    public int getMaxSize() {
        String value = read("mes.mail.retry.queue.max", "MES_MAIL_RETRY_QUEUE_MAX");
        int maxSize = parseInt(value, 200);
        return Math.max(50, maxSize);
    }

    /**
     * 목적: 재시도 큐에 항목을 적재한다.
     * 기능: 최대 크기를 초과하면 오래된 항목을 제거한다.
     * 이유: 최근 실패 건을 우선 복구하기 위함이다.
     * 유지보수: 큐 정책 변경 시 로직을 수정한다.
     */
    public synchronized void enqueue(EmailRetryItem item) {
        if (!isEnabled() || item == null) {
            return;
        }
        int maxSize = getMaxSize();
        while (queue.size() >= maxSize) {
            queue.pollFirst();
        }
        queue.addLast(item);
    }

    /**
     * 목적: 재시도 큐 크기를 조회한다.
     * 기능: 현재 적재된 항목 수를 반환한다.
     * 이유: 관리자 화면 표시용으로 사용하기 위함이다.
     * 유지보수: 큐 구현 변경 시 로직을 수정한다.
     */
    public synchronized int size() {
        return queue.size();
    }

    /**
     * 목적: 큐에 적재된 항목을 모두 꺼낸다.
     * 기능: 큐를 비우고 처리 대상 목록을 반환한다.
     * 이유: 재시도 시 중복 처리를 방지하기 위함이다.
     * 유지보수: 재시도 정책 변경 시 로직을 수정한다.
     */
    public synchronized List<EmailRetryItem> drainAll() {
        List<EmailRetryItem> items = new ArrayList<EmailRetryItem>(queue);
        queue.clear();
        return items;
    }

    /**
     * 목적: 설정 값을 읽는다.
     * 기능: 시스템 속성/환경 변수 순으로 조회한다.
     * 이유: 환경별 설정을 유연하게 적용하기 위함이다.
     * 유지보수: 설정 키 변경 시 로직을 수정한다.
     */
    private String read(String sysKey, String envKey) {
        String value = System.getProperty(sysKey);
        if (value == null || value.trim().isEmpty()) {
            value = System.getenv(envKey);
        }
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    /**
     * 목적: 문자열을 정수로 변환한다.
     * 기능: 변환 실패 시 기본값을 반환한다.
     * 이유: 잘못된 설정으로 인한 오류를 방지하기 위함이다.
     * 유지보수: 기본값 정책 변경 시 수정한다.
     */
    private int parseInt(String value, int fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
