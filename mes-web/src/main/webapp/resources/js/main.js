// 목적: 공통 스크립트를 제공한다. 기능: CRUD 테스트용 API 호출을 제공한다. 이유: 화면에서 빠르게 동작을 검증하기 위함이다. 유지보수: 공통 API 호출 규칙이 바뀌면 여기서 수정한다.
(function () {
  function toParams(formId) {
    var form = document.getElementById(formId);
    if (!form) {
      return new URLSearchParams();
    }
    return new URLSearchParams(new FormData(form));
  }

  function render(resultId, payload) {
    var target = document.getElementById(resultId);
    if (!target) {
      return;
    }
    target.textContent = JSON.stringify(payload, null, 2);
  }

  function post(url, formId, resultId) {
    fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8" },
      body: toParams(formId).toString()
    })
      .then(function (response) { return response.json(); })
      .then(function (data) { render(resultId, data); })
      .catch(function (error) { render(resultId, { result: "fail", message: String(error) }); });
  }

  window.MesWeb = {
    post: post
  };
})();
