package com.mes.web.dao;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mes.web.dao.mapper.WorkMapper;

/**
 * 목적: 작업 데이터 접근을 담당한다.
 * 기능: MyBatis 매퍼를 통해 작업 조회를 수행한다.
 * 이유: 서비스 계층과 SQL 매퍼를 분리하기 위함이다.
 * 유지보수: 쿼리 변경 시 매퍼 XML과 함께 수정한다.
 */
@Repository
public class WorkDao {

    private final WorkMapper workMapper;

    /**
     * 목적: 매퍼를 주입받는다.
     * 기능: DAO 내부에서 매퍼를 사용할 수 있게 한다.
     * 이유: 데이터 접근을 인터페이스로 분리하기 위함이다.
     * 유지보수: 매퍼 교체 시 주입만 변경한다.
     */
    @Autowired
    public WorkDao(WorkMapper workMapper) {
        this.workMapper = workMapper;
    }

    /**
     * 목적: 작업 목록을 조회한다.
     * 기능: 매퍼를 호출해 결과 목록을 반환한다.
     * 이유: 서비스 계층에서 데이터를 사용하기 위함이다.
     * 유지보수: 필터 확정 시 criteria 구조를 구체화한다.
     */
    public List<Map<String, Object>> selectWorkOrders(Map<String, Object> criteria) {
        if (workMapper == null) {
            return Collections.emptyList();
        }
        return workMapper.selectWorkOrders(criteria);
    }
}
