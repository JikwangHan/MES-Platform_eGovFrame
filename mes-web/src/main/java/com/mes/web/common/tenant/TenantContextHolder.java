package com.mes.web.common.tenant;

/**
 * 목적: 현재 요청의 테넌트 ID를 스레드 단위로 보관한다.
 * 기능: 요청 시작 시 설정하고, 종료 시 해제한다.
 * 이유: 멀티테넌트 DB 라우팅을 안전하게 하기 위함이다.
 * 유지보수: 테넌트 키 규칙 변경 시 이 클래스만 수정한다.
 */
public final class TenantContextHolder {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<String>();

    private TenantContextHolder() {
    }

    /**
     * 목적: 현재 요청의 테넌트 ID를 저장한다.
     * 기능: ThreadLocal에 테넌트 값을 설정한다.
     * 이유: DB 라우팅 시 현재 테넌트를 참조하기 위함이다.
     * 유지보수: 입력 값 검증 규칙이 생기면 여기서 처리한다.
     */
    public static void setTenantId(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * 목적: 현재 요청의 테넌트 ID를 조회한다.
     * 기능: ThreadLocal 값을 반환한다.
     * 이유: 라우팅 데이터소스가 테넌트를 결정할 때 사용한다.
     * 유지보수: 기본값 처리 로직이 필요하면 이 메서드에 추가한다.
     */
    public static String getTenantId() {
        return CURRENT_TENANT.get();
    }

    /**
     * 목적: 요청 종료 시 테넌트 정보를 제거한다.
     * 기능: ThreadLocal 값을 초기화한다.
     * 이유: 스레드 재사용 시 값 누수를 막기 위함이다.
     * 유지보수: 특별한 종료 처리 규칙이 생기면 여기에 추가한다.
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
