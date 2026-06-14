from __future__ import annotations

import json
import logging
import subprocess
import threading
import time

from .config import Settings
from .storage import OutboxStorage


LOGGER = logging.getLogger(__name__)


class LocalSubscriber:
    def __init__(self, settings: Settings, storage: OutboxStorage) -> None:
        self.settings = settings
        self.storage = storage
        self._stop_event = threading.Event()
        self._thread: threading.Thread | None = None
        self._process: subprocess.Popen[str] | None = None

    def start(self) -> None:
        LOGGER.info(
            "Starting local subscriber via mosquitto_sub on %s:%s topic=%s",
            self.settings.local_broker_host,
            self.settings.local_broker_port,
            self.settings.local_topic_filter,
        )
        self._thread = threading.Thread(target=self._run, name="local-subscriber", daemon=True)
        self._thread.start()

    def stop(self) -> None:
        self._stop_event.set()
        if self._process and self._process.poll() is None:
            self._process.terminate()
            try:
                self._process.wait(timeout=3)
            except subprocess.TimeoutExpired:
                self._process.kill()
        if self._thread:
            self._thread.join(timeout=3)

    def _run(self) -> None:
        while not self._stop_event.is_set():
            command = [
                "mosquitto_sub",
                "-h",
                self.settings.local_broker_host,
                "-p",
                str(self.settings.local_broker_port),
                "-t",
                self.settings.local_topic_filter,
                "-v",
            ]
            if self.settings.local_broker_username:
                command.extend(["-u", self.settings.local_broker_username])
            if self.settings.local_broker_password:
                command.extend(["-P", self.settings.local_broker_password])

            LOGGER.info("Launching local subscriber process")
            self._process = subprocess.Popen(
                command,
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                text=True,
                bufsize=1,
            )

            assert self._process.stdout is not None
            for raw_line in self._process.stdout:
                if self._stop_event.is_set():
                    break
                self._handle_line(raw_line.strip())

            if self._stop_event.is_set():
                break

            stderr_output = ""
            if self._process.stderr is not None:
                stderr_output = self._process.stderr.read().strip()
            LOGGER.warning("Local subscriber process exited. stderr=%s", stderr_output)
            time.sleep(1)

    def _handle_line(self, line: str) -> None:
        if not line or " " not in line:
            return
        source_topic, payload_text = line.split(" ", 1)
        try:
            payload_data = json.loads(payload_text)
        except json.JSONDecodeError as exc:
            LOGGER.error("Discarding invalid JSON from topic %s: %s", source_topic, exc)
            return

        cloud_topic = self._build_cloud_topic(source_topic, payload_data)
        message_id = self.storage.add_message(
            source_topic=source_topic,
            cloud_topic=cloud_topic,
            payload=payload_text,
            qos=self.settings.publish_qos,
        )
        LOGGER.info(
            "Stored inbound message id=%s source_topic=%s cloud_topic=%s",
            message_id,
            source_topic,
            cloud_topic,
        )

    def _build_cloud_topic(self, source_topic: str, payload_data: dict) -> str:
        if self.settings.cloud_topic:
            return self.settings.cloud_topic

        prefix = self.settings.cloud_topic_prefix.strip()
        device_id = (
            payload_data.get("metadados", {}).get("device_id")
            if isinstance(payload_data.get("metadados"), dict)
            else None
        )

        if source_topic == "telemetry":
            clean_prefix = prefix.rstrip("/")
            if device_id:
                return f"{clean_prefix}/telemetry/{device_id}"
            return f"{clean_prefix}/telemetry/unknown-device"

        if not prefix:
            return source_topic
        clean_prefix = prefix.rstrip("/")
        return f"{clean_prefix}/{source_topic}"
