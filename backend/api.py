"""pywebview JS bridge — exposes the backend API to the frontend.

The frontend calls ``window.pywebview_api.call(jsonPayload)`` and receives
synchronous-ish results via the returned value.  All methods are thin
adapters over the service / settings / security modules.
"""
import json
import threading
from typing import Any, Dict, List, Optional

from . import cloud, security
from .config import APP_NAME, APP_VERSION, SUPPORT_EMAIL
from .services import (
    AiService,
    UpdateResult,
    compute_update_result,
    load_domains,
    load_services,
)
from .settings import SettingsManager


def _serialize_services(services: List[AiService]) -> List[Dict[str, Any]]:
    return [
        {
            "name": s.name,
            "url": s.url,
            "category": s.category,
            "pricing": s.pricing,
            "privacy": s.privacy,
            "loginRequired": s.login_required,
            "bestFor": list(s.best_for),
            "accentColor": s.accent_color,
        }
        for s in services
    ]


class Api:
    """JS-accessible API.  Methods are invoked as ``api.<method>(params)``."""

    def __init__(self, settings: SettingsManager):
        self.settings = settings
        self._services: List[AiService] = []
        self._domains = set()
        self._lock = threading.Lock()
        self._refresh_state()

    # ------------------------------------------------------------------
    def _refresh_state(self) -> None:
        self._services = load_services()
        self._domains = load_domains()
        security.init_security(self._domains)

    # ------------------------------------------------------------------
    def call(self, payload: str) -> str:
        """Main entry point — dispatches a JSON-RPC-ish request."""
        try:
            req = json.loads(payload)
            method = req.get("method")
            params = req.get("params") or {}
            handler = getattr(self, f"_{method}", None)
            if handler is None:
                return json.dumps({"error": f"unknown method: {method}"})
            result = handler(params)
            return json.dumps({"result": result}, default=str)
        except Exception as exc:  # pragma: no cover - defensive
            return json.dumps({"error": str(exc)})

    # ------------------------------------------------------------------
    # catalogue
    # ------------------------------------------------------------------
    def _get_services(self, _params) -> List[Dict[str, Any]]:
        return _serialize_services(self._services)

    def _get_service(self, params) -> Optional[Dict[str, Any]]:
        name = params.get("name")
        for s in self._services:
            if s.name == name:
                return _serialize_services([s])[0]
        return None

    def _get_categories(self, _params) -> List[str]:
        return sorted({s.category for s in self._services})

    def _get_all_data(self, _params) -> Dict[str, Any]:
        """Snapshot used by the frontend on startup."""
        return {
            "services": _serialize_services(self._services),
            "settings": self.settings.to_dict(),
            "onboardingCompleted": self.settings.onboarding_completed,
            "version": APP_VERSION,
            "app_name": APP_NAME,
        }

    def _update_catalog(self, params) -> Dict[str, Any]:
        """Manual catalogue refresh — the ONLY way the catalogue is refreshed."""
        changed_services = cloud.update_services()
        changed_domains = cloud.update_domains()
        if changed_services or changed_domains:
            old = self._services
            self._refresh_state()
            new = self._services
            result = compute_update_result(old, new)
            self.settings.save_services_last_updated_date()
            self.settings.save_domains_last_updated_date()
            return {
                "changed": True,
                "servicesChanged": changed_services,
                "domainsChanged": changed_domains,
                "added": _serialize_services(result.added),
                "removed": _serialize_services(result.removed),
                "modified": [
                    {"service": _serialize_services([m["service"]])[0], "changes": m["changes"]}
                    for m in result.modified
                ],
                "newCategories": result.new_categories,
            }
        return {"changed": False}

    def _restore_bundled(self, _params) -> Dict[str, Any]:
        cloud.restore_bundled()
        self._refresh_state()
        self.settings.save_services_last_updated_date()
        self.settings.save_domains_last_updated_date()
        return {"restored": True}

    def _get_catalog_status(self, _params) -> Dict[str, Any]:
        from datetime import date
        return {
            "servicesLastUpdated": self.settings.get_services_last_updated_date(),
            "domainsLastUpdated": self.settings.get_domains_last_updated_date(),
            "servicesCount": len(self._services),
            "domainsCount": len(self._domains),
            "today": date.today().isoformat(),
        }

    # ------------------------------------------------------------------
    # settings
    # ------------------------------------------------------------------
    def _save_settings(self, params) -> Dict[str, Any]:
        for k, v in params.items():
            self.settings.set(k, v)
        return {"ok": True}

    def _get_settings(self, _params) -> Dict[str, Any]:
        return self.settings.to_dict()

    def _set_onboarding_completed(self, params) -> Dict[str, Any]:
        self.settings.set_onboarding_completed(bool(params.get("value", True)))
        return {"ok": True}

    def _save_last_opened_service(self, params) -> Dict[str, Any]:
        self.settings.save_last_opened_service(params.get("name", ""))
        return {"ok": True}

    # ------------------------------------------------------------------
    # external
    # ------------------------------------------------------------------
    def _open_external(self, params) -> Dict[str, Any]:
        import webbrowser
        url = params.get("url", "")
        if url:
            webbrowser.open(url)
        return {"ok": True}

    def _share_link(self, params) -> Dict[str, Any]:
        # Best-effort: copy to clipboard and open mailto
        import subprocess
        text = params.get("text", "")
        try:
            subprocess.run(["clip"], input=text.encode("utf-16-le"), check=False)
        except Exception:
            pass
        return {"ok": True}

    # ------------------------------------------------------------------
    # info
    # ------------------------------------------------------------------
    def _get_app_info(self, _params) -> Dict[str, Any]:
        return {
            "name": APP_NAME,
            "version": APP_VERSION,
            "supportEmail": SUPPORT_EMAIL,
            "github": "https://github.com/SilentCoderHere/aihub",
        }