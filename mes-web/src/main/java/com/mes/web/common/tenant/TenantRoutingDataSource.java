package com.mes.web.common.tenant;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 목적: 테넌트 ID에 따라 실제 데이터소스를 선택한다.
 * 기능: ThreadLocal에 저장된 테넌트 키를 반환한다.
 * 이유: 기업별 DB 분리 원칙을 구현하기 위함이다.
 * 유지보수: 테넌트 키 규칙이 변경되면 반환 로직을 수정한다.
 */
public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    /**
     * 목적: 현재 요청에 맞는 테넌트 키를 반환한다.
     * 기능: TenantContextHolder에 저장된 값을 반환한다.
     * 이유: Spring이 라우팅 데이터소스를 결정하도록 하기 위함이다.
     * 유지보수: 기본값/예외 처리 정책은 여기서 조정한다.
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContextHolder.getTenantId();
    }
}
