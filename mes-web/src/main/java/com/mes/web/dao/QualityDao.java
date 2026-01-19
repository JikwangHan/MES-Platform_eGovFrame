package com.mes.web.dao;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mes.web.dao.mapper.QualityMapper;

/**
 * 목적: 품질 데이터 접근을 담당한다.
 * 기능: 불량 조회/등록/삭제를 수행한다.
 * 이유: 서비스 계층과 SQL 매퍼를 분리하기 위함이다.
 * 유지보수: 쿼리 변경 시 매퍼 XML과 함께 수정한다.
 */
@Repository
public class QualityDao {

    private final QualityMapper qualityMapper;

    /**
     * 목적: 매퍼를 주입받는다.
     * 기능: DAO 내부에서 매퍼를 사용할 수 있게 한다.
     * 이유: 데이터 접근을 인터페이스로 분리하기 위함이다.
     * 유지보수: 매퍼 교체 시 주입만 변경한다.
     */
    @Autowired
    public QualityDao(QualityMapper qualityMapper) {
        this.qualityMapper = qualityMapper;
    }

    /**
     * 목적: 불량 내역을 조회한다.
     * 기능: 매퍼를 호출해 결과 목록을 반환한다.
     * 이유: 서비스 계층에서 데이터를 사용하기 위함이다.
     * 유지보수: 필터 확정 시 criteria 구조를 구체화한다.
     */
    public List<Map<String, Object>> selectDefects(Map<String, Object> criteria) {
        return qualityMapper.selectDefects(criteria);
    }

    /**
     * 목적: 불량을 등록한다.
     * 기능: 불량 정보를 저장한다.
     * 이유: 등록 기능을 제공하기 위함이다.
     * 유지보수: 필수 컬럼 확정 시 파라미터를 보완한다.
     */
    public int insertDefect(Map<String, Object> defect) {
        return qualityMapper.insertDefect(defect);
    }

    /**
     * 목적: 불량을 삭제한다.
     * 기능: 불량 ID 기준으로 삭제한다.
     * 이유: 삭제 기능을 제공하기 위함이다.
     * 유지보수: 삭제 정책 변경 시 SQL을 수정한다.
     */
    public int deleteDefect(long id) {
        return qualityMapper.deleteDefect(id);
    }
}
