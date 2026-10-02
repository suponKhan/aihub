"""AI service catalogue — load, filter, and persist the service list."""
import json
import os
from dataclasses import dataclass, field
from pathlib import Path
from typing import Dict, List, Optional, Set


@dataclass
class AiService:
    name: str
    url: str
    category: str
    pricing: str
    privacy: str
    login_required: bool
    best_for: List[str] = field(default_factory=list)
    accent_color: str = "#6750A4"


@dataclass
class UpdateResult:
    added: List[AiService]
    removed: List[AiService]
    modified: List[dict]
    new_categories: List[str]


def _accent_from_name(name: str) -> str:
    """Deterministic HSL-ish colour from service name (mirrors Android impl)."""
    h = hash(name) & 0xFFFFFFFF
    hue = (h % 360)
    lightness = 0.55 + ((h // 360) % 30) / 100.0
    c = (1 - abs(2 * lightness - 1)) * 0.7
    x = c * (1 - abs((hue / 60.0) % 2 - 1))
    m = lightness - c / 2
    if hue < 60:
        r, g, b = c, x, 0.0
    elif hue < 120:
        r, g, b = x, c, 0.0
    elif hue < 180:
        r, g, b = 0.0, c, x
    elif hue < 240:
        r, g, b = 0.0, x, c
    elif hue < 300:
        r, g, b = x, 0.0, c
    else:
        r, g, b = c, 0.0, x
    r = int((r + m) * 255)
    g = int((g + m) * 255)
    b = int((b + m) * 255)
    return f"#{r:02X}{g:02X}{b:02X}"


def load_services(path: Optional[Path] = None) -> List[AiService]:
    """Load services from a JSON file in the original Android format."""
    from .config import SERVICES_LOCAL, SERVICES_FILE

    p = Path(path) if path else (SERVICES_LOCAL if SERVICES_LOCAL.exists() else SERVICES_FILE)
    if not p.exists():
        return []
    try:
        with open(p, "r", encoding="utf-8") as fh:
            raw_map = json.load(fh)
    except (json.JSONDecodeError, OSError):
        return []

    services: List[AiService] = []
    for category_name, raw_list in raw_map.items():
        for raw in raw_list:
            services.append(AiService(
                name=raw["name"],
                url=raw["website"],
                category=category_name,
                pricing=raw.get("pricing", "Unknown"),
                privacy=raw.get("privacy", "Unknown"),
                login_required=bool(raw.get("login_required", False)),
                best_for=list(raw.get("best_for", [])),
                accent_color=_accent_from_name(raw["name"]),
            ))
    return services


def load_domains(path: Optional[Path] = None) -> Set[str]:
    """Load blocked domains from a plain-text file (one host per line)."""
    from .config import DOMAINS_LOCAL, DOMAINS_FILE

    p = Path(path) if path else (DOMAINS_LOCAL if DOMAINS_LOCAL.exists() else DOMAINS_FILE)
    if not p.exists():
        return set()
    try:
        with open(p, "r", encoding="utf-8") as fh:
            return {line.strip() for line in fh if line.strip()}
    except OSError:
        return set()


def compute_update_result(old: List[AiService], new: List[AiService]) -> UpdateResult:
    """Diff two service lists (mirrors Android performServiceUpdate)."""
    old_map = {s.name: s for s in old}
    new_map = {s.name: s for s in new}

    added = [s for s in new if s.name not in old_map]
    removed = [s for s in old if s.name not in new_map]

    modified = []
    for old_s in old:
        new_s = new_map.get(old_s.name)
        if not new_s:
            continue
        changes = []
        if old_s.url != new_s.url:
            changes.append(f"URL: {old_s.url} → {new_s.url}")
        if old_s.pricing != new_s.pricing:
            changes.append(f"Pricing: {old_s.pricing} → {new_s.pricing}")
        if old_s.privacy != new_s.privacy:
            changes.append(f"Privacy: {old_s.privacy} → {new_s.privacy}")
        if old_s.login_required != new_s.login_required:
            old_val = "Yes" if old_s.login_required else "No"
            new_val = "Yes" if new_s.login_required else "No"
            changes.append(f"Login Required: {old_val} → {new_val}")
        if old_s.best_for != new_s.best_for:
            old_str = ", ".join(old_s.best_for)
            new_str = ", ".join(new_s.best_for)
            changes.append(f"Best For: {old_str} → {new_str}")
        if changes:
            modified.append({"service": new_s, "changes": changes})

    new_categories = list({s.category for s in new} - {s.category for s in old})
    return UpdateResult(added=added, removed=removed, modified=modified, new_categories=new_categories)