"""Constants and configuration paths for AI Hub Windows port."""
import os
from pathlib import Path

APP_NAME = "AI Hub"
APP_VERSION = "3.0.1"
PACKAGE_NAME = "com.foss.aihub"

# Data directory — persistent across runs
DATA_DIR = Path.home() / ".aihub_windows"
DATA_DIR.mkdir(parents=True, exist_ok=True)

# Bundled config (shipped with the app, refreshed manually)
BUNDLED_DIR = Path(__file__).resolve().parent.parent / "data"
SERVICES_FILE = BUNDLED_DIR / "ais.json"
DOMAINS_FILE = BUNDLED_DIR / "domains.txt"

# Local state files
SERVICES_LOCAL = DATA_DIR / "ais.json"
DOMAINS_LOCAL = DATA_DIR / "domains.txt"
SETTINGS_FILE = DATA_DIR / "settings.json"
DOMAINS_ETAG_FILE = DATA_DIR / "domains_etag.txt"
SERVICES_ETAG_FILE = DATA_DIR / "services_etag.txt"

# Cloud update sources
CLOUD_BASE_URL = "https://silentcoderhere.github.io/aihub-config-data/"
GITHUB_USER_NAME = "SilentCoderHere"
GITHUB_REPO_NAME = "aihub"
SUPPORT_EMAIL = "silentcoder@tutamail.com"

# User agents
USER_AGENT_DESKTOP = (
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
    "(KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36"
)
USER_AGENT_MOBILE = (
    "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 "
    "(KHTML, like Gecko) Chrome/145.0.7632.121 Mobile Safari/537.36"
)

# Default settings
DEFAULT_SETTINGS = {
    "theme": "auto",
    "loadLastOpenedAI": True,
    "multipleDefaultAi": False,
    "defaultServiceName": None,
    "defaultServiceNames": [],
    "serviceOrder": [],
    "enabledServices": [],
    "favoriteServices": [],
    "maxKeepAlive": 5,
    "enableZoom": True,
    "desktopView": False,
    "thirdPartyCookies": False,
    "fontSizePercentage": 100,
    "updateFrequencyDays": 3,
    "blockAdsAndTrackers": True,
    "checkForUpdate": True,
    "isProxy": False,
    "proxyType": "http",
    "proxyHost": "localhost",
    "proxyPort": "9050",
    "customCss": "",
    "customJs": "",
    "filterCategories": [],
    "filterPrices": [],
    "filterPrivacy": [],
    "filterLoginRequired": None,
    "enableNewServicesByDefault": False,
    "preferredCategories": [],
    "preferredPrices": [],
    "preferredPrivacy": [],
    "preferredLoginRequired": None,
    "onboardingCompleted": False,
    "domainsLastUpdatedDate": None,
    "servicesLastUpdatedDate": None,
    "lastUpdateCheckDate": None,
}