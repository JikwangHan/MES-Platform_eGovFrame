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
     * 목적: 아이디 중복 여부를 확인한다.
     * 기능: user_id 기준으로 등록된 사용자 수를 반환한다.
     * 이유: 회원가입 중복을 방지하기 위함이다.
     * 유지보수: 중복 기준 변경 시 SQL을 수정한다.
     */
    int countByUserId(@Param("userId") String userId);

    /**
     * 목적: 신규 사용자 정보를 저장한다.
     * 기능: 회원가입 입력값을 DB에 저장한다.
     * 이유: 인증 대상 사용자를 생성하기 위함이다.
     * 유지보수: 컬럼 추가 시 SQL을 수정한다.
     */
    void insertUser(@Param("payload") java.util.Map<String, Object> payload);

    /**
     * 목적: 사용자 상태를 갱신한다.
     * 기능: user_id 기준으로 status를 업데이트한다.
     * 이유: 인증/승인 흐름에 필요한 상태 전환을 지원하기 위함이다.
     * 유지보수: 상태 정책 변경 시 SQL을 수정한다.
     */
    void updateUserStatus(@Param("userId") String userId, @Param("status") String status);

    /**
     * 목적: 승인 대기 사용자 목록을 조회한다.
     * 기능: pending_approval 상태의 사용자를 반환한다.
     * 이유: 관리자 승인 화면에 표시하기 위함이다.
     * 유지보수: 상태 기준이 변경되면 SQL을 수정한다.
     */
    java.util.List<java.util.Map<String, Object>> findPendingApprovalUsers();

    /**
     * 목적: 사용자 이메일(암호문)을 조회한다.
     * 기능: user_id 기준으로 암호화된 이메일을 반환한다.
     * 이유: 승인 결과 알림 발송에 사용하기 위함이다.
     * 유지보수: 컬럼/암호화 정책 변경 시 SQL을 수정한다.
     */
    String findEncryptedEmailByUserId(@Param("userId") String userId);

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
