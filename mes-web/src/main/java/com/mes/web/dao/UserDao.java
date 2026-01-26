package com.mes.web.dao;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mes.web.dao.mapper.UserMapper;

/**
 * 목적: 사용자 데이터 접근을 담당한다.
 * 기능: 사용자 조회 및 로그인 상태 업데이트를 수행한다.
 * 이유: 인증 로직과 DB 접근을 분리하기 위함이다.
 * 유지보수: 쿼리 변경 시 매퍼 XML과 함께 수정한다.
 */
@Repository
public class UserDao {

    private final UserMapper userMapper;

    /**
     * 목적: 매퍼를 주입받는다.
     * 기능: DAO 내부에서 매퍼를 사용할 수 있게 한다.
     * 이유: 데이터 접근을 인터페이스로 분리하기 위함이다.
     * 유지보수: 매퍼 교체 시 주입만 변경한다.
     */
    @Autowired
    public UserDao(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 목적: 사용자 정보를 조회한다.
     * 기능: user_id 기준으로 사용자 정보를 반환한다.
     * 이유: 로그인 인증에 필요한 정보를 얻기 위함이다.
     * 유지보수: 컬럼 확장 시 반환 구조를 보완한다.
     */
    public Map<String, Object> findByUserId(String userId) {
        return userMapper.findByUserId(userId);
    }

    /**
     * 목적: 사용자 아이디 중복 여부를 확인한다.
     * 기능: user_id 기준으로 등록된 사용자 수를 반환한다.
     * 이유: 회원가입 시 중복 아이디를 막기 위함이다.
     * 유지보수: 중복 기준 변경 시 SQL을 조정한다.
     */
    public int countByUserId(String userId) {
        return userMapper.countByUserId(userId);
    }

    /**
     * 목적: 신규 사용자 정보를 저장한다.
     * 기능: 회원가입 입력값을 DB에 저장한다.
     * 이유: 인증 대상 사용자를 생성하기 위함이다.
     * 유지보수: 컬럼 추가 시 매퍼와 함께 수정한다.
     */
    public void insertUser(Map<String, Object> payload) {
        userMapper.insertUser(payload);
    }

    /**
     * 목적: 사용자 상태를 갱신한다.
     * 기능: user_id 기준으로 status를 업데이트한다.
     * 이유: 이메일 인증/승인 처리 흐름에 필요하기 때문이다.
     * 유지보수: 상태 값 정책 변경 시 SQL을 조정한다.
     */
    public void updateUserStatus(String userId, String status) {
        userMapper.updateUserStatus(userId, status);
    }

    /**
     * 목적: 로그인 성공 정보를 기록한다.
     * 기능: 로그인 성공 시간과 실패 횟수를 초기화한다.
     * 이유: 잠금 정책을 일관되게 유지하기 위함이다.
     * 유지보수: 정책 변경 시 SQL을 조정한다.
     */
    public void recordLoginSuccess(String userId) {
        userMapper.recordLoginSuccess(userId);
    }

    /**
     * 목적: 로그인 실패 정보를 기록한다.
     * 기능: 실패 횟수를 증가시키고 필요 시 잠금 처리한다.
     * 이유: 계정 잠금 정책을 적용하기 위함이다.
     * 유지보수: 임계값 변경 시 SQL과 호출부를 수정한다.
     */
    public void recordLoginFail(String userId, int lockThreshold) {
        userMapper.recordLoginFail(userId, lockThreshold);
    }
}
