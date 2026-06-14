from __future__ import annotations

import logging
import signal
import time

from .cloud_forwarder import CloudForwarder
from .config import load_settings
from .local_subscriber import LocalSubscriber
from .storage import OutboxStorage


def configure_logging(level: str) -> None:
    logging.basicConfig(
        level=getattr(logging, level.upper(), logging.INFO),
        format="%(asctime)s %(levelname)s %(name)s: %(message)s",
    )


def main() -> int:
    settings = load_settings()
    configure_logging(settings.log_level)

    logger = logging.getLogger("tvbox_gateway")
    storage = OutboxStorage(settings.sqlite_path)
    subscriber = LocalSubscriber(settings, storage)
    forwarder = CloudForwarder(settings, storage)
    running = True

    def handle_signal(signum, frame) -> None:
        nonlocal running
        logger.info("Received signal %s. shutting down.", signum)
        running = False

    signal.signal(signal.SIGINT, handle_signal)
    signal.signal(signal.SIGTERM, handle_signal)

    subscriber.start()
    logger.info("Gateway started with pending_count=%s", storage.pending_count())

    try:
        while running:
            sent = forwarder.flush_pending()
            if sent:
                logger.info("Flush cycle complete. sent=%s pending=%s", sent, storage.pending_count())
            forwarder.publish_status()
            time.sleep(settings.forward_interval_seconds)
    finally:
        subscriber.stop()
        forwarder.disconnect()
        logger.info("Gateway stopped")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
