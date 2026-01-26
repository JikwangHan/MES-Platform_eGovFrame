package com.mes.web.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.mes.web.common.crypto.CryptoService;
import com.mes.web.dao.UserDao;
import com.mes.web.service.UserRegistrationService;

/**
 * 목적: 회원가입 처리 로직을 구현한다.
 * 기능: 입력값 검증, 중복 체크, 암호화/해시 후 DB 저장을 수행한다.
 * 이유: 보안 정책(해시/암호화)을 만족하는 가입 흐름을 제공하기 위함이다.
 * 유지보수: 정책 변경 시 검증/암호화 로직을 조정한다.
 */
@Service
public class UserRegistrationServiceImpl implements UserRegistrationService {

    private final UserDao userDao;
    private final CryptoService cryptoService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 목적: 필요한 의존성을 주입받는다.
     * 기능: 사용자 DAO와 암호화 서비스를 연결한다.
     * 이유: 데이터 접근과 암호화를 분리해 유지보수를 쉽게 하기 위함이다.
     * 유지보수: 구현체 변경 시 주입만 교체한다.
     */
    @Autowired
    public UserRegistrationServiceImpl(UserDao userDao, CryptoService cryptoService) {
        this.userDao = userDao;
        this.cryptoService = cryptoService;
    }

    /**
     * 목적: 신규 사용자를 등록한다.
     * 기능: 입력값 검증, 중복 체크, 암호화/해시 처리, DB 저장을 수행한다.
     * 이유: 회원가입의 보안/무결성을 보장하기 위함이다.
     * 유지보수: 필드/정책 변경 시 검증 로직을 보완한다.
     */
    @Override
    public void registerUser(String userId,
                             String userName,
                             String password,
                             String role,
                             String phone,
                             String email,
                             String status) {
        validate(userId, userName, password, role);
        if (!isBlank(email) && !email.contains("@")) {
            throw new IllegalArgumentException("이메일 형식이 올바르지 않습니다.");
        }
        if (userDao.countByUserId(userId) > 0) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        String passwordHash = passwordEncoder.encode(password);
        String userNameEnc = cryptoService.encrypt(userName, userId);
        String phoneEnc = isBlank(phone) ? null : cryptoService.encrypt(phone, userId);
        String emailEnc = isBlank(email) ? null : cryptoService.encrypt(email, userId);

        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("userId", userId);
        payload.put("userName", userNameEnc);
        payload.put("passwordHash", passwordHash);
        payload.put("role", normalizeRole(role));
        payload.put("status", normalizeStatus(status));
        payload.put("phone", phoneEnc);
        payload.put("email", emailEnc);
        userDao.insertUser(payload);
    }

    /**
     * 목적: 필수 입력값을 검증한다.
     * 기능: 아이디/이름/비밀번호/역할의 최소 규칙을 검사한다.
     * 이유: 잘못된 데이터 저장을 방지하기 위함이다.
     * 유지보수: 정책 변경 시 검증 규칙을 조정한다.
     */
    private void validate(String userId, String userName, String password, String role) {
        if (isBlank(userId) || userId.length() < 4 || userId.length() > 32) {
            throw new IllegalArgumentException("아이디는 4~32자 이내로 입력해 주세요.");
        }
        if (isBlank(userName)) {
            throw new IllegalArgumentException("이름을 입력해 주세요.");
        }
        if (isBlank(password) || password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상으로 입력해 주세요.");
        }
        String normalizedRole = normalizeRole(role);
        if (!"OPERATOR".equals(normalizedRole) && !"VIEWER".equals(normalizedRole)) {
            throw new IllegalArgumentException("허용되지 않은 역할입니다.");
        }
    }

    /**
     * 목적: 역할 값을 표준화한다.
     * 기능: 비어있으면 기본 역할(OPERATOR)로 변환한다.
     * 이유: DB 저장 시 역할 값을 일관되게 유지하기 위함이다.
     * 유지보수: 기본 역할 정책이 바뀌면 여기서 변경한다.
     */
    private String normalizeRole(String role) {
        if (isBlank(role)) {
            return "OPERATOR";
        }
        return role.trim().toUpperCase();
    }

    /**
     * 목적: 상태 값을 표준화한다.
     * 기능: 비어있으면 active로 변환하고 허용 값만 저장한다.
     * 이유: 회원 상태를 일관되게 관리하기 위함이다.
     * 유지보수: 상태 체계 변경 시 여기서 보완한다.
     */
    private String normalizeStatus(String status) {
        if (isBlank(status)) {
            return "active";
        }
        String normalized = status.trim().toLowerCase();
        if ("active".equals(normalized) || "pending".equals(normalized) || "pending_email".equals(normalized)
                || "pending_approval".equals(normalized)) {
            return normalized;
        }
        return "active";
    }

    /**
     * 목적: 공백 문자열을 판별한다.
     * 기능: null/빈 문자열/공백만 있는 문자열을 true로 반환한다.
     * 이유: 입력값 검증을 단순화하기 위함이다.
     * 유지보수: 유틸 공통화 시 이동한다.
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
