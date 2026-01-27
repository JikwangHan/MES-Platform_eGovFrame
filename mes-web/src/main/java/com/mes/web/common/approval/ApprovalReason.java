package com.mes.web.common.approval;

/**
 * 목적: 승인/반려 사유 코드를 표현한다.
 * 기능: 코드와 표시명을 보관한다.
 * 이유: 사유를 표준화해 운영 일관성을 높이기 위함이다.
 * 유지보수: 사유 코드 추가 시 이 클래스를 확장한다.
 */
public class ApprovalReason {

    private final String code;
    private final String label;

    /**
     * 목적: 사유 코드를 생성한다.
     * 기능: 코드와 표시명을 초기화한다.
     * 이유: 화면/로그에 일관된 값을 사용하기 위함이다.
     * 유지보수: 필드 확장 시 생성자를 보완한다.
     */
    public ApprovalReason(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 목적: 사유 코드 값을 반환한다.
     * 기능: 저장/로그에 사용할 코드를 제공한다.
     * 이유: 코드 기반 처리에 사용하기 위함이다.
     * 유지보수: 코드 체계 변경 시 호출부를 점검한다.
     */
    public String getCode() {
        return code;
    }

    /**
     * 목적: 사유 표시명을 반환한다.
     * 기능: 화면 표시용 텍스트를 제공한다.
     * 이유: 사용자에게 이해 가능한 문구를 제공하기 위함이다.
     * 유지보수: 표기 정책 변경 시 값을 수정한다.
     */
    public String getLabel() {
        return label;
    }
}
