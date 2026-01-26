package com.mes.web.common.auth;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.mes.web.common.crypto.CryptoException;
import com.mes.web.common.crypto.CryptoService;

/**
 * 목적: 개발 단계용 로그인 검증을 제공한다.
 * 기능: DB 사용자 조회 및 비밀번호 해시 검증을 수행한다.
 * 이유: 보안 정책(해시 저장)을 준수하기 위함이다.
 * 유지보수: 정책 변경 시 DAO/해시 로직을 조정한다.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final int LOCK_THRESHOLD = 5;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final com.mes.web.dao.UserDao userDao;
    private final CryptoService cryptoService;

    /**
     * 목적: 사용자 DAO를 주입받는다.
     * 기능: 사용자 조회 및 로그인 기록을 DAO로 처리한다.
     * 이유: 인증 로직과 데이터 접근을 분리하기 위함이다.
     * 유지보수: DAO 교체 시 주입만 변경한다.
     */
    @Autowired
    public AuthServiceImpl(com.mes.web.dao.UserDao userDao, CryptoService cryptoService) {
        this.userDao = userDao;
        this.cryptoService = cryptoService;
    }

    /**
     * 목적: DB 사용자 정보를 기반으로 로그인 여부를 판단한다.
     * 기능: 비밀번호 해시 검증 및 잠금 정책을 적용한다.
     * 이유: 보안 기준(해시 저장, 잠금 정책)을 지키기 위함이다.
     * 유지보수: 잠금 임계값/정책 변경 시 상수를 조정한다.
     */
    @Override
    public AuthUser authenticate(String userId, String password) {
        Map<String, Object> user = userDao.findByUserId(userId);
        if (user == null) {
            LOGGER.warn("로그인 실패(사용자 없음): {}", userId);
            return devFallback(userId, password);
        }
        String status = String.valueOf(user.get("status"));
        if ("locked".equalsIgnoreCase(status)) {
            LOGGER.warn("로그인 실패(잠금 상태): {}", userId);
            return null;
        }
        if (!"active".equalsIgnoreCase(status)) {
            LOGGER.warn("로그인 실패(비활성 상태): {} 상태={}", userId, status);
            return null;
        }
        String passwordHash = String.valueOf(user.get("password_hash"));
        if (!passwordEncoder.matches(password, passwordHash)) {
            userDao.recordLoginFail(userId, LOCK_THRESHOLD);
            LOGGER.warn("로그인 실패(비밀번호 오류): {}", userId);
            return null;
        }
        userDao.recordLoginSuccess(userId);
        String userName = String.valueOf(user.get("user_name"));
        String resolvedName = userName;
        try {
            resolvedName = cryptoService.decrypt(userName);
        } catch (CryptoException ex) {
            LOGGER.debug("사용자 이름 복호화 실패, 원문 값 사용: {}", ex.getMessage());
        }
        String role = String.valueOf(user.get("role"));
        LOGGER.info("로그인 성공: {}", userId);
        return new AuthUser(userId, resolvedName, role);
    }

    /**
     * 목적: 개발 단계에서 임시 로그인 경로를 제공한다.
     * 기능: 시스템 속성으로 devMode가 true일 때만 허용한다.
     * 이유: 초기 UI 테스트를 빠르게 진행하기 위함이다.
     * 유지보수: 운영 전 devMode 비활성 상태를 유지한다.
     */
    private AuthUser devFallback(String userId, String password) {
        boolean devMode = Boolean.parseBoolean(System.getProperty("mes.auth.devMode", "false"));
        if (devMode && "admin".equals(userId) && "admin".equals(password)) {
            LOGGER.warn("개발 모드 임시 로그인 허용: {}", userId);
            return new AuthUser(userId, "개발관리자", "SYSTEM_ADMIN");
        }
        return null;
    }
}
