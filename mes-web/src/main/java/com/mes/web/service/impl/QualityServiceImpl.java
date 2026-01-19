package com.mes.web.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mes.web.dao.QualityDao;
import com.mes.web.service.QualityService;

/**
 * 목적: 품질 서비스의 기본 구현을 제공한다.
 * 기능: 임시로 빈 목록을 반환한다.
 * 이유: 화면 골격 개발을 빠르게 진행하기 위함이다.
 * 유지보수: 불량 통계/그래프 연동 시 확장한다.
 */
@Service
public class QualityServiceImpl implements QualityService {

    private final QualityDao qualityDao;

    /**
     * 목적: DAO를 주입받는다.
     * 기능: 서비스에서 DAO를 사용할 수 있게 한다.
     * 이유: 데이터 접근과 비즈니스 로직을 분리하기 위함이다.
     * 유지보수: DAO 교체 시 주입만 변경한다.
     */
    @Autowired
    public QualityServiceImpl(QualityDao qualityDao) {
        this.qualityDao = qualityDao;
    }

    /**
     * 목적: 불량 내역을 조회한다.
     * 기능: DAO를 통해 불량 내역을 조회한다.
     * 이유: DB 연동 구조를 미리 확보하기 위함이다.
     * 유지보수: 조건 확정 시 criteria 구조를 보완한다.
     */
    @Override
    public List<Map<String, Object>> findDefects(Map<String, Object> criteria) {
        return qualityDao.selectDefects(criteria);
    }
}
