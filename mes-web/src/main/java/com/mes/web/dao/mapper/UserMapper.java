package com.mes.web.dao.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Param;

/**
 * 목적: 사용자 매퍼 인터페이스를 정의한다.
 * 기능: 사용자 조회 및 로그인 상태 갱신 SQL을 선언한다.
 * 이유: 인증 로직과 SQL을 분리하기 위함이다.
 * 유지보수: 정책 변경 시 SQL과 함께 수정한다.
 */
public interface UserMapper {

    /**
     * 목적: 사용자 정보를 조회한다.
     * 기능: user_id 기준으로 사용자 정보를 반환한다.
     * 이유: 로그인 인증에 필요한 정보를 얻기 위함이다.
     * 유지보수: 컬럼 확정 시 반환 구조를 보완한다.
     */
    Map<String, Object> findByUserId(@Param("userId") String userId);

    /**
     * 목적: 로그인 성공 기록을 남긴다.
     * 기능: 마지막 로그인 시간과 실패 횟수를 초기화한다.
     * 이유: 계정 잠금 정책을 정상화하기 위함이다.
     * 유지보수: 정책 변경 시 SQL을 수정한다.
     */
    void recordLoginSuccess(@Param("userId") String userId);

    /**
     * 목적: 로그인 실패 기록을 남긴다.
     * 기능: 실패 횟수를 증가시키고 임계값 이상이면 잠금 처리한다.
     * 이유: 보안 정책을 적용하기 위함이다.
     * 유지보수: 임계값 정책 변경 시 로직을 수정한다.
     */
    void recordLoginFail(@Param("userId") String userId, @Param("lockThreshold") int lockThreshold);
}
