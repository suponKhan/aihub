"""AI Hub — Windows desktop port.

Entry point:  ``python backend/app.py``
"""
import json
import os
import sys
import threading
from pathlib import Path
from typing import Optional

# Ensure bundled config is importable
sys.path.insert(0, str(Path(__file__).resolve().parent))

import webview  # noqa: E402

from .api import Api  # noqa: E402
from .config import APP_NAME, APP_VERSION, DATA_DIR  # noqa: E402
from .settings import SettingsManager  # noqa: E402


def _read_file(path: Path) -> str:
    try:
        return path.read_text(encoding="utf-8")
    except OSError:
        return ""


def _build_index_html() -> str:
    """Compose the full HTML document from the template + frontend files."""
    base = Path(__file__).resolve().parent.parent / "frontend"
    template = _read_file(base / "index.html")

    css = _read_file(base / "css" / "style.css")
    js = _read_file(base / "js" / "app.js")
    components = ""
    screens_dir = base / "screens"
    if screens_dir.exists():
        for p in sorted(screens_dir.glob("*.html")):
            components += f"\n<!-- screen:{p.stem} -->\n{_read_file(p)}\n"

    html = template
    html = html.replace("{{CSS}}", css)
    html = html.replace("{{JS}}", js)
    html = html.replace("{{SCREENS}}", components)
    return html


class HubApi:
    """Adapter that wraps the backend Api for pywebview's js_api."""

    def __init__(self, api: Api):
        self._api = api

    def call(self, payload: str) -> str:
        return self._api.call(payload)


def main() -> None:
    settings = SettingsManager()

    # Restore bundled data if local data is missing
    from . import cloud
    if not (DATA_DIR / "ais.json").exists():
        cloud.restore_bundled()

    api = Api(settings)
    html = _build_index_html()

    window = webview.create_window(
        APP_NAME,
        html=html,
        width=1280,
        height=860,
        x=100,
        y=100,
        min_size=(900, 600),
        frameless=False,
        on_top=False,
        confirm_close=False,
        js_api=HubApi(api),
    )

    webview.start(debug=False)


if __name__ == "__main__":
    main()