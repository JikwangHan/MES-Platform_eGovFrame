"""
MES-Platform | IoT-Edge-Gateway PoC 스크립트
===========================================

이 파일의 목적
--------------
- 이 스크립트는 **Python으로 구현한 간단한 PoC(개념 검증용) Edge Gateway 예제**입니다.
- 실제 제품용 Go 구현 전에, Modbus 장비에서 데이터를 읽고
  표준 형태(Envelope)로 만들어 MQTT/REST로 전송하는 흐름을 빠르게 검증하기 위한 코드입니다.

기능 (최소 범위)
----------------
- Modbus Read (주소 3개 이상 읽기)
- Envelope(Telemetry) 생성 (protocolVersion, schemaVersion 필수 포함)
- MQTT 또는 REST로 전송 (둘 중 하나만 또는 둘 다 사용 가능)
- 수신/전송 결과를 로그로 남김
- 오류 3종 처리: 타임아웃, 주소 오류, 인증 실패(전송 측) 시 예외 발생

유지보수/확장 관점
------------------
- 실제 환경에서는 Python이 아닌 Go 기반 Edge Gateway가 주력이 되며,
  이 스크립트는 **PoC나 디버깅 용도**로만 사용하는 것을 권장합니다.
- 설정값(IP, 포트, 주소맵 등)을 하드코딩하지 않고 `config/` 디렉터리의 파일에서
  읽어오도록 바꾸고 싶다면, 상단의 전역 상수들을 제거하고 별도의 설정 로딩 함수를
  추가한 뒤 `read_modbus` / `send_via_mqtt` / `send_via_rest`에서 그 값을 사용하도록
  수정하면 됩니다.
- Go 버전과 기능을 맞추고 싶다면, Envelope 구조(필드 이름, 버전 등)를 동일하게 유지해야 합니다.
"""

from __future__ import annotations

import json
import logging
from dataclasses import dataclass
from datetime import datetime, timezone
from typing import Any, Dict, List, Optional

import requests
from pymodbus.client import ModbusTcpClient

try:
    import paho.mqtt.client as mqtt  # type: ignore

    HAS_MQTT = True
except Exception:
    HAS_MQTT = False


# 기본 설정 (추후 config 파일 연동 예정)
PROTOCOL_VERSION = "1.0"
SCHEMA_VERSION = "1.0"

MODBUS_HOST = "127.0.0.1"
MODBUS_PORT = 502
MODBUS_SLAVE_ID = 1

# 예시용 주소 3개 이상 (0/1 기반 여부는 실제 장비 기준으로 조정)
MODBUS_ADDRESSES = [
    {"name": "RUN", "address": 0, "count": 1},
    {"name": "QTY", "address": 1, "count": 1},
    {"name": "TEMP_RAW", "address": 2, "count": 1},
]

MQTT_ENABLED = True
MQTT_BROKER = "127.0.0.1"
MQTT_PORT = 1883
MQTT_TOPIC = "mes/edge/telemetry"

REST_ENABLED = False
REST_ENDPOINT = "http://127.0.0.1:18080/api/edge/telemetry"
REST_AUTH_TOKEN: Optional[str] = None  # 인증 실패 테스트 시 사용


logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
)


@dataclass
class Envelope:
    protocolVersion: str
    schemaVersion: str
    messageType: str  # Telemetry / Event / Command / Ack
    deviceId: str
    timestamp: str
    payload: Dict[str, Any]

    def to_json(self) -> str:
        return json.dumps(self.__dict__, ensure_ascii=False)


def _iso_timestamp() -> str:
    # PoC 목적: 간단히 UTC ISO8601 사용 (타임존 포함 여부는 향후 합의)
    return datetime.now(timezone.utc).isoformat()


def read_modbus() -> Dict[str, Any]:
    """
    Modbus에서 최소 3개 주소를 읽어 dict로 반환.
    오류 발생 시 예외를 발생시켜 상위에서 처리/격리.
    """
    client = ModbusTcpClient(MODBUS_HOST, port=MODBUS_PORT)
    if not client.connect():
        raise ConnectionError(f"Modbus 연결 실패: {MODBUS_HOST}:{MODBUS_PORT}")

    try:
        result: Dict[str, Any] = {}
        for item in MODBUS_ADDRESSES:
            name = item["name"]
            address = item["address"]
            count = item["count"]

            # PoC: Holding Register 기준 (실제는 정의서 기반 조정)
            rr = client.read_holding_registers(address=address, count=count, slave=MODBUS_SLAVE_ID)
            if rr.isError():
                raise ValueError(f"Modbus 주소 오류 또는 응답 오류: {name} (addr={address})")

            # 단일 값만 사용 (count > 1 인 경우 확장 예정)
            value = rr.registers[0]
            result[name] = value

        # 예시: TEMP_RAW를 실제 온도 값으로 변환하는 로직은 후속 설계에 따라 추가
        return result
    finally:
        client.close()


def build_telemetry_envelope(device_id: str, payload: Dict[str, Any]) -> Envelope:
    return Envelope(
        protocolVersion=PROTOCOL_VERSION,
        schemaVersion=SCHEMA_VERSION,
        messageType="Telemetry",
        deviceId=device_id,
        timestamp=_iso_timestamp(),
        payload=payload,
    )


def send_via_mqtt(envelope: Envelope) -> None:
    if not MQTT_ENABLED:
        logging.info("MQTT 전송 비활성화(MQTT_ENABLED=False)")
        return
    if not HAS_MQTT:
        raise RuntimeError("paho-mqtt 미설치: requirements.txt 기반으로 설치 필요")

    client = mqtt.Client()
    try:
        client.connect(MQTT_BROKER, MQTT_PORT, keepalive=30)
        payload = envelope.to_json().encode("utf-8")
        result = client.publish(MQTT_TOPIC, payload, qos=0)
        result.wait_for_publish(timeout=5)
        if result.rc != mqtt.MQTT_ERR_SUCCESS:
            raise RuntimeError(f"MQTT publish 실패: rc={result.rc}")
        logging.info("MQTT 전송 성공: topic=%s", MQTT_TOPIC)
    finally:
        client.disconnect()


def send_via_rest(envelope: Envelope) -> None:
    if not REST_ENABLED:
        logging.info("REST 전송 비활성화(REST_ENABLED=False)")
        return

    headers = {"Content-Type": "application/json"}
    if REST_AUTH_TOKEN:
        # 인증 실패 시나리오 재현을 위해 잘못된 토큰을 넣는 것도 가능
        headers["Authorization"] = f"Bearer {REST_AUTH_TOKEN}"

    resp = requests.post(REST_ENDPOINT, data=envelope.to_json().encode("utf-8"), headers=headers, timeout=5)
    if not resp.ok:
        raise RuntimeError(f"REST 전송 실패: status={resp.status_code}, body={resp.text}")
    logging.info("REST 전송 성공: status=%s", resp.status_code)


def main() -> None:
    """
    단일 사이클:
    - Modbus Read (3개 이상 주소)
    - Telemetry Envelope 생성
    - MQTT / REST 전송
    """
    device_id = "EDGE-DEVICE-001"  # PoC용, 실제는 설정에서 로드 예정
    try:
        raw_values = read_modbus()
        envelope = build_telemetry_envelope(device_id=device_id, payload=raw_values)
        logging.info("Envelope 생성: %s", envelope.to_json())

        # 전송 경로 선택 (둘 다 활성화 가능)
        send_via_mqtt(envelope)
        send_via_rest(envelope)

        logging.info("PoC 사이클 완료")
    except Exception as exc:
        # 실제 구현 시: logs/ 및 quarantine/에 원본/오류 정보 저장 로직 추가 예정
        logging.error("PoC 실행 중 오류 발생: %s", exc, exc_info=True)
        raise


if __name__ == "__main__":
    main()

