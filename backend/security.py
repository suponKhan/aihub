"""Domain security — block trackers / ads (mirrors Android WebViewSecurity)."""
from pathlib import Path
from typing import Set
from urllib.parse import urlparse

from .config import DOMAINS_LOCAL, DOMAINS_FILE
from .services import load_domains


class WebViewSecurity:
    """Tracks the set of blocked domains and decides whether a URL is allowed."""

    def __init__(self, domains: Optional[Set[str]] = None):
        self._domains: Set[str] = domains if domains is not None else load_domains()

    @property
    def domains(self) -> Set[str]:
        return self._domains

    def reload(self) -> None:
        self._domains = load_domains()

    def allow_connectivity(self, url: str, block: bool = True) -> bool:
        if not block:
            return True
        if not url:
            return False
        lower = url.lower()
        if any(lower.startswith(p) for p in ("blob:", "about:blank", "data:", "file:", "content:")):
            return True
        parsed = urlparse(url)
        host = (parsed.host or "").lower()
        if not host:
            return False
        if parsed.scheme != "https":
            return False
        if host in self._domains:
            return False
        return True


# Module-level helper used by the JS bridge
_default_security: "WebViewSecurity | None" = None


def get_security() -> "WebViewSecurity":
    global _default_security
    if _default_security is None:
        _default_security = WebViewSecurity()
    return _default_security


def init_security(domains: Set[str]) -> None:
    global _default_security
    _default_security = WebViewSecurity(domains)