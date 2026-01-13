package main

import (
	"encoding/json"
	"log"
	"net/http"
	"net/url"
	"regexp"
	"time"
)

// LogRequest는 스마트공장 로그 수집 API 입력 규격을 따릅니다.
// 목적: 실제 서버와 동일한 포맷으로 가상 서버 연동을 검증합니다.
type LogRequest struct {
	CrtfcKey   string `json:"crtfcKey"`
	LogDt      string `json:"logDt"`
	UseSe      string `json:"useSe"`
	SysUser    string `json:"sysUser"`
	ConectIP   string `json:"conectIp"`
	DataUsgqty *int   `json:"dataUsgqty"`
}

// LogResponse는 수신 결과 응답 포맷을 맞추기 위한 구조체입니다.
// 목적: 실제 서버와 동일한 결과코드/메시지 구조로 클라이언트 검증을 지원합니다.
type LogResponse struct {
	RecptnDt     string `json:"recptnDt"`
	RecptnRsltCd string `json:"recptnRsltCd"`
	RecptnRslt   string `json:"recptnRslt"`
	RecptnRsltDtl string `json:"recptnRsltDtl"`
}

// timeRegex는 로그일시 형식(YYYY-MM-DD HH:MI:SS.SSS) 검증에 사용됩니다.
// 유지보수 관점: 규격 변경 시 이 정규식만 수정하면 됩니다.
var timeRegex = regexp.MustCompile(`^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3}$`)

// main은 가상 로그 수집 API 서버를 실행합니다.
// 목적: 실서버 연동 전 동일한 엔드포인트로 기능을 검증합니다.
func main() {
	http.HandleFunc("/apisvc/sendLogData.json", handleJSON)
	http.HandleFunc("/apisvc/sendLogDataJSON.do", handleJSONParam)

	log.Println("Mock Log API listening on :18090")
	log.Fatal(http.ListenAndServe(":18090", nil))
}

// handleJSON은 JSON 본문 전송 방식을 처리합니다.
// 목적: 표준 JSON 전송 경로를 가장 단순한 방식으로 검증합니다.
func handleJSON(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		writeResult(w, "AP1010", "기타 오류", "POST만 허용")
		return
	}
	var req LogRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeResult(w, "AP1023", "기타 오류 (JSON 데이터 형식 오류)", "JSON 파싱 실패")
		return
	}
	validateAndRespond(w, req)
}

// handleJSONParam은 logData=JSON 문자열 방식 전송을 처리합니다.
// 목적: 실서버에서 지원하는 URL 인코딩 JSON 전송을 모사합니다.
func handleJSONParam(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		writeResult(w, "AP1010", "기타 오류", "POST만 허용")
		return
	}
	if err := r.ParseForm(); err != nil {
		writeResult(w, "AP1025", "기타 오류 (데이터 전송 오류)", "form 파싱 실패")
		return
	}
	raw := r.FormValue("logData")
	if raw == "" {
		writeResult(w, "AP1025", "기타 오류 (데이터 전송 오류)", "logData 없음")
		return
	}
	decoded, _ := url.QueryUnescape(raw)
	var req LogRequest
	if err := json.Unmarshal([]byte(decoded), &req); err != nil {
		writeResult(w, "AP1023", "기타 오류 (JSON 데이터 형식 오류)", "JSON 파싱 실패")
		return
	}
	validateAndRespond(w, req)
}

// validateAndRespond는 필수 필드와 포맷을 검증하고 결과를 반환합니다.
// 이유: 규격 위반 시 결과코드를 명확히 돌려서 클라이언트 보정을 돕습니다.
func validateAndRespond(w http.ResponseWriter, req LogRequest) {
	if req.CrtfcKey == "" {
		writeResult(w, "AP1011", "기타 오류 (API 인증키 데이터 없음)", "")
		return
	}
	if req.LogDt == "" {
		writeResult(w, "AP1012", "기타 오류 (로그 일시 데이터 없음)", "")
		return
	}
	if !timeRegex.MatchString(req.LogDt) {
		writeResult(w, "AP1014", "기타 오류 (로그 일시 데이터가 지정된 날짜 형식이 아님)", "")
		return
	}
	if req.UseSe == "" {
		writeResult(w, "AP1015", "기타 오류 (사용 구분 데이터 없음)", "")
		return
	}
	if req.SysUser == "" {
		writeResult(w, "AP1017", "기타 오류 (시스템 사용자 데이터 없음)", "")
		return
	}
	if req.ConectIP == "" {
		writeResult(w, "AP1019", "기타 오류 (접속 IP 데이터 없음)", "")
		return
	}
	if req.DataUsgqty == nil {
		writeResult(w, "AP1022", "기타 오류 (데이터 사용량 데이터가 공백)", "")
		return
	}

	writeResult(w, "AP1002", "데이터 이관 완료", "")
}

// writeResult는 공통 응답 형식을 생성합니다.
// 목적: 모든 응답이 동일한 구조를 유지하도록 중앙에서 관리합니다.
func writeResult(w http.ResponseWriter, code, msg, detail string) {
	resp := LogResponse{
		RecptnDt:     time.Now().Format("2006-01-02 15:04:05.000"),
		RecptnRsltCd: code,
		RecptnRslt:   msg,
		RecptnRsltDtl: detail,
	}
	w.Header().Set("Content-Type", "application/json")
	_ = json.NewEncoder(w).Encode(resp)
}
