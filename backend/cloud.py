"""Cloud catalogue update — downloads ais.json / domains.txt with ETag caching."""
import urllib.request
import urllib.error
from pathlib import Path
from typing import Optional, Tuple

from .config import (
    CLOUD_BASE_URL,
    DOMAINS_ETAG_FILE,
    DOMAINS_LOCAL,
    DOMAINS_FILE,
    SERVICES_ETAG_FILE,
    SERVICES_LOCAL,
    SERVICES_FILE,
)


def _read_etag(path: Path) -> Optional[str]:
    if path.exists():
        return path.read_text(encoding="utf-8").strip() or None
    return None


def _write_etag(path: Path, etag: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(etag, encoding="utf-8")


def _fetch(url: str, etag: Optional[str]) -> Optional[Tuple[str, Optional[str]]]:
    """GET with conditional ETag.  Returns (body, new_etag) or None if 304."""
    req = urllib.request.Request(url, method="HEAD")
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            new_etag = resp.headers.get("ETag")
    except urllib.error.URLError:
        return None

    if etag is not None and new_etag == etag:
        return None  # not modified

    req = urllib.request.Request(url, method="GET")
    try:
        with urllib.request.urlopen(req, timeout=20) as resp:
            body = resp.read().decode("utf-8", errors="replace")
            final_etag = resp.headers.get("ETag") or new_etag
            return body, final_etag
    except urllib.error.URLError:
        return None


def update_domains() -> bool:
    """Refresh domains.txt from the cloud.  Returns True if changed."""
    etag = _read_etag(DOMAINS_ETAG_FILE)
    result = _fetch(CLOUD_BASE_URL + "domains.txt", etag)
    if result is None:
        return False
    body, new_etag = result
    DOMAINS_LOCAL.parent.mkdir(parents=True, exist_ok=True)
    DOMAINS_LOCAL.write_text(body, encoding="utf-8")
    if new_etag:
        _write_etag(DOMAINS_ETAG_FILE, new_etag)
    return True


def update_services() -> bool:
    """Refresh ais.json from the cloud.  Returns True if changed."""
    etag = _read_etag(SERVICES_ETAG_FILE)
    result = _fetch(CLOUD_BASE_URL + "ais.json", etag)
    if result is None:
        return False
    body, new_etag = result
    SERVICES_LOCAL.parent.mkdir(parents=True, exist_ok=True)
    SERVICES_LOCAL.write_text(body, encoding="utf-8")
    if new_etag:
        _write_etag(SERVICES_ETAG_FILE, new_etag)
    return True


def restore_bundled() -> None:
    """Copy the bundled config files into the local data dir (offline fallback)."""
    if SERVICES_FILE.exists():
        SERVICES_LOCAL.parent.mkdir(parents=True, exist_ok=True)
        SERVICES_LOCAL.write_bytes(SERVICES_FILE.read_bytes())
    if DOMAINS_FILE.exists():
        DOMAINS_LOCAL.parent.mkdir(parents=True, exist_ok=True)
        DOMAINS_LOCAL.write_bytes(DOMAINS_FILE.read_bytes())