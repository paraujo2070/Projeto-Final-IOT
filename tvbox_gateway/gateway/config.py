from __future__ import annotations

import os
from dataclasses import dataclass


def _env(name: str, default: str | None = None) -> str | None:
    value = os.getenv(name)
    if value is None or value == "":
        return default
    return value


def _env_int(name: str, default: int) -> int:
    raw = _env(name)
    if raw is None:
        return default
    return int(raw)


def _env_float(name: str, default: float) -> float:
    raw = _env(name)
    if raw is None:
        return default
    return float(raw)


@dataclass(frozen=True)
class Settings:
    gateway_id: str
    sqlite_path: str
    local_broker_host: str
    local_broker_port: int
    local_broker_username: str | None
    local_broker_password: str | None
    local_topic_filter: str
    local_client_id: str
    cloud_broker_host: str
    cloud_broker_port: int
    cloud_broker_username: str | None
    cloud_broker_password: str | None
    cloud_client_id: str
    cloud_topic_prefix: str
    cloud_topic: str | None
    cloud_payload_format: str
    forward_interval_seconds: float
    publish_qos: int
    status_topic: str
    log_level: str


def load_settings() -> Settings:
    gateway_id = _env("GATEWAY_ID", "tvbox-gateway") or "tvbox-gateway"
    return Settings(
        gateway_id=gateway_id,
        sqlite_path=_env("SQLITE_PATH", "/var/lib/tvbox-gateway/outbox.db") or "/var/lib/tvbox-gateway/outbox.db",
        local_broker_host=_env("LOCAL_BROKER_HOST", "127.0.0.1") or "127.0.0.1",
        local_broker_port=_env_int("LOCAL_BROKER_PORT", 1883),
        local_broker_username=_env("LOCAL_BROKER_USERNAME"),
        local_broker_password=_env("LOCAL_BROKER_PASSWORD"),
        local_topic_filter=_env("LOCAL_TOPIC_FILTER", "telemetry") or "telemetry",
        local_client_id=_env("LOCAL_CLIENT_ID", f"{gateway_id}-local-subscriber") or f"{gateway_id}-local-subscriber",
        cloud_broker_host=_env("CLOUD_BROKER_HOST", "test.mosquitto.org") or "test.mosquitto.org",
        cloud_broker_port=_env_int("CLOUD_BROKER_PORT", 1883),
        cloud_broker_username=_env("CLOUD_BROKER_USERNAME"),
        cloud_broker_password=_env("CLOUD_BROKER_PASSWORD"),
        cloud_client_id=_env("CLOUD_CLIENT_ID", f"{gateway_id}-cloud-forwarder") or f"{gateway_id}-cloud-forwarder",
        cloud_topic_prefix=_env("CLOUD_TOPIC_PREFIX", "cloud/") or "cloud/",
        cloud_topic=_env("CLOUD_TOPIC"),
        cloud_payload_format=_env("CLOUD_PAYLOAD_FORMAT", "raw") or "raw",
        forward_interval_seconds=_env_float("FORWARD_INTERVAL_SECONDS", 5.0),
        publish_qos=_env_int("PUBLISH_QOS", 1),
        status_topic=_env("STATUS_TOPIC", f"gateway/{gateway_id}/status") or f"gateway/{gateway_id}/status",
        log_level=_env("LOG_LEVEL", "INFO") or "INFO",
    )
