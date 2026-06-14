from __future__ import annotations

import os
import sqlite3
from datetime import datetime, timezone

from .models import OutboxMessage


class OutboxStorage:
    def __init__(self, db_path: str) -> None:
        self.db_path = db_path
        parent_dir = os.path.dirname(db_path)
        if parent_dir:
            os.makedirs(parent_dir, exist_ok=True)
        self._initialize()

    def _connect(self) -> sqlite3.Connection:
        conn = sqlite3.connect(self.db_path)
        conn.row_factory = sqlite3.Row
        return conn

    def _initialize(self) -> None:
        with self._connect() as conn:
            conn.execute(
                """
                CREATE TABLE IF NOT EXISTS outbox (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    source_topic TEXT NOT NULL,
                    cloud_topic TEXT NOT NULL,
                    payload TEXT NOT NULL,
                    qos INTEGER NOT NULL,
                    received_at TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'pending',
                    retry_count INTEGER NOT NULL DEFAULT 0,
                    last_error TEXT
                )
                """
            )
            conn.commit()

    def add_message(self, source_topic: str, cloud_topic: str, payload: str, qos: int) -> int:
        received_at = datetime.now(timezone.utc).isoformat()
        with self._connect() as conn:
            cursor = conn.execute(
                """
                INSERT INTO outbox (source_topic, cloud_topic, payload, qos, received_at, status)
                VALUES (?, ?, ?, ?, ?, 'pending')
                """,
                (source_topic, cloud_topic, payload, qos, received_at),
            )
            conn.commit()
            return int(cursor.lastrowid)

    def get_pending_messages(self, limit: int = 100) -> list[OutboxMessage]:
        with self._connect() as conn:
            rows = conn.execute(
                """
                SELECT id, source_topic, cloud_topic, payload, qos, received_at, retry_count, last_error
                FROM outbox
                WHERE status = 'pending'
                ORDER BY id ASC
                LIMIT ?
                """,
                (limit,),
            ).fetchall()
        return [OutboxMessage(**dict(row)) for row in rows]

    def mark_sent(self, message_id: int) -> None:
        with self._connect() as conn:
            conn.execute("DELETE FROM outbox WHERE id = ?", (message_id,))
            conn.commit()

    def mark_failed(self, message_id: int, error: str) -> None:
        with self._connect() as conn:
            conn.execute(
                """
                UPDATE outbox
                SET retry_count = retry_count + 1,
                    last_error = ?
                WHERE id = ?
                """,
                (error[:500], message_id),
            )
            conn.commit()

    def pending_count(self) -> int:
        with self._connect() as conn:
            row = conn.execute(
                "SELECT COUNT(*) AS count FROM outbox WHERE status = 'pending'"
            ).fetchone()
        return int(row["count"])
