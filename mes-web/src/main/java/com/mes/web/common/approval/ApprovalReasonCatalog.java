package com.mes.web.common.approval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 목적: 승인/반려 사유 코드 목록을 제공한다.
 * 기능: 표준 사유 코드를 리스트로 반환한다.
 * 이유: 코드화/표준화를 통해 운영 일관성을 확보하기 위함이다.
 * 유지보수: 사유 코드 추가/변경 시 리스트를 수정한다.
 */
public final class ApprovalReasonCatalog {

    private static final List<ApprovalReason> REASONS;

    static {
        List<ApprovalReason> list = new ArrayList<ApprovalReason>();
        list.add(new ApprovalReason("APPROVE_GENERAL", "승인(일반)"));
        list.add(new ApprovalReason("REJECT_DOCS", "서류 미비"));
        list.add(new ApprovalReason("REJECT_DUPLICATE", "중복/허위 정보"));
        list.add(new ApprovalReason("REJECT_POLICY", "정책 위반"));
        list.add(new ApprovalReason("REJECT_OTHER", "기타"));
        REASONS = Collections.unmodifiableList(list);
    }

    private ApprovalReasonCatalog() {
    }

    /**
     * 목적: 표준 사유 코드 목록을 제공한다.
     * 기능: 승인/반려 사유 리스트를 반환한다.
     * 이유: 화면/로그에서 동일한 코드를 사용하기 위함이다.
     * 유지보수: 코드 체계 변경 시 여기만 수정한다.
     */
    public static List<ApprovalReason> list() {
        return REASONS;
    }
}
