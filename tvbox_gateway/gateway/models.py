from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class OutboxMessage:
    id: int
    source_topic: str
    cloud_topic: str
    payload: str
    qos: int
    received_at: str
    retry_count: int
    last_error: str | None
