"""Settings persistence (JSON-backed, mirrors Android SharedPreferences)."""
import json
import os
from pathlib import Path
from typing import Any, Dict, List, Optional, Set

from .config import DEFAULT_SETTINGS, SETTINGS_FILE


class SettingsManager:
    """Persistent settings store.  Reads/writes a single JSON file."""

    def __init__(self, settings_path: Optional[Path] = None):
        self._path = settings_path or SETTINGS_FILE
        self._settings: Dict[str, Any] = {}
        self._load()

    # ------------------------------------------------------------------
    # internal helpers
    # ------------------------------------------------------------------
    def _load(self) -> None:
        if self._path.exists():
            try:
                with open(self._path, "r", encoding="utf-8") as fh:
                    self._settings = json.load(fh)
            except (json.JSONDecodeError, OSError):
                self._settings = {}
        # merge defaults for any missing keys
        merged = dict(DEFAULT_SETTINGS)
        merged.update(self._settings)
        self._settings = merged
        self._save()

    def _save(self) -> None:
        self._path.parent.mkdir(parents=True, exist_ok=True)
        with open(self._path, "w", encoding="utf-8") as fh:
            json.dump(self._settings, fh, indent=2, ensure_ascii=False)

    # ------------------------------------------------------------------
    # public API
    # ------------------------------------------------------------------
    def get(self, key: str, default: Any = None) -> Any:
        return self._settings.get(key, default)

    def set(self, key: str, value: Any) -> None:
        self._settings[key] = value
        self._save()

    def update(self, fn) -> None:
        fn(self._settings)
        self._save()

    # ---- typed accessors ------------------------------------------------
    @property
    def theme(self) -> str:
        return self.get("theme", "auto")

    @property
    def load_last_opened_ai(self) -> bool:
        return bool(self.get("loadLastOpenedAI", True))

    @property
    def enabled_services(self) -> Set[str]:
        return set(self.get("enabledServices", []))

    @property
    def favorite_services(self) -> Set[str]:
        return set(self.get("favoriteServices", []))

    @property
    def service_order(self) -> List[str]:
        return list(self.get("serviceOrder", []))

    @property
    def max_keep_alive(self) -> int:
        return int(self.get("maxKeepAlive", 5))

    @property
    def desktop_view(self) -> bool:
        return bool(self.get("desktopView", False))

    @property
    def third_party_cookies(self) -> bool:
        return bool(self.get("thirdPartyCookies", False))

    @property
    def font_size_percentage(self) -> int:
        return int(self.get("fontSizePercentage", 100))

    @property
    def block_ads_and_trackers(self) -> bool:
        return bool(self.get("blockAdsAndTrackers", True))

    @property
    def is_proxy(self) -> bool:
        return bool(self.get("isProxy", False))

    @property
    def proxy_type(self) -> str:
        return self.get("proxyType", "http")

    @property
    def proxy_host(self) -> str:
        return self.get("proxyHost", "localhost")

    @property
    def proxy_port(self) -> str:
        return self.get("proxyPort", "9050")

    @property
    def custom_css(self) -> str:
        return self.get("customCss", "")

    @property
    def custom_js(self) -> str:
        return self.get("customJs", "")

    @property
    def onboarding_completed(self) -> bool:
        return bool(self.get("onboardingCompleted", False))

    def set_onboarding_completed(self, value: bool = True) -> None:
        self.set("onboardingCompleted", value)

    def save_last_opened_service(self, name: str) -> None:
        self.set("lastOpenedService", name)

    def get_last_opened_service(self) -> Optional[str]:
        return self.get("lastOpenedService")

    # ---- date helpers ---------------------------------------------------
    def _get_or_today(self, key: str) -> str:
        from datetime import date
        val = self.get(key)
        if val:
            return val
        today = date.today().isoformat()
        self.set(key, today)
        return today

    def get_domains_last_updated_date(self) -> str:
        return self._get_or_today("domainsLastUpdatedDate")

    def save_domains_last_updated_date(self, value: Optional[str] = None) -> None:
        from datetime import date
        self.set("domainsLastUpdatedDate", value or date.today().isoformat())

    def get_services_last_updated_date(self) -> str:
        return self._get_or_today("servicesLastUpdatedDate")

    def save_services_last_updated_date(self, value: Optional[str] = None) -> None:
        from datetime import date
        self.set("servicesLastUpdatedDate", value or date.today().isoformat())

    def get_last_update_check_date(self) -> str:
        return self._get_or_today("lastUpdateCheckDate")

    def save_last_update_check_date(self, value: Optional[str] = None) -> None:
        from datetime import date
        self.set("lastUpdateCheckDate", value or date.today().isoformat())

    # ---- etags ----------------------------------------------------------
    def get_domains_etag(self) -> Optional[str]:
        return self.get("domainsETag")

    def save_domains_etag(self, etag: str) -> None:
        self.set("domainsETag", etag)

    def get_services_etag(self) -> Optional[str]:
        return self.get("servicesETag")

    def save_services_etag(self, etag: str) -> None:
        self.set("servicesETag", etag)

    def to_dict(self) -> Dict[str, Any]:
        return dict(self._settings)