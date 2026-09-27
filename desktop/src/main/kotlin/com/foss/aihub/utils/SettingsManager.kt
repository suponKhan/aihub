package com.foss.aihub.pc.utils

import com.foss.aihub.pc.models.AppSettings
import java.io.File
import java.util.prefs.Preferences

class SettingsManager(val dataDir: File) {
    private val prefs = Preferences.userNodeForPackage(SettingsManager::class.java)
    private val settingsFile = File(dataDir, "settings.json")

    var settings: AppSettings
        get() = if (settingsFile.exists()) {
            try {
                jsonFormat.decodeFromString<AppSettings>(settingsFile.readText())
            } catch (_: Exception) {
                AppSettings()
            }
        } else AppSettings()
        set(value) {
            settingsFile.writeText(jsonFormat.encodeToString(value))
        }

    fun updateSettings(update: (AppSettings) -> Unit) {
        val current = settings
        update(current)
        settings = current
    }

    fun saveEnabledServices(services: Set<String>) {
        settings = settings.copy(enabledServices = services)
    }

    fun loadEnabledServices(): Set<String> = settings.enabledServices
    fun saveServiceOrder(order: List<String>) {
        settings = settings.copy(serviceOrder = order)
    }

    fun loadServiceOrder(): List<String> = settings.serviceOrder

    fun saveFavoriteServices(favorites: Set<String>) {
        settings = settings.copy(favoriteServices = favorites)
    }

    fun loadFavoriteServices(): Set<String> = settings.favoriteServices

    fun saveLastOpenedService(serviceName: String) {
        prefs.put("lastOpenedService", serviceName)
    }

    fun getLastOpenedService(): String? = prefs.get("lastOpenedService", null)

    fun saveLastUpdateCheckDate(date: String = "") {
        prefs.put("lastUpdateCheck", date)
    }

    fun getLastUpdateCheckDate(): String = prefs.get("lastUpdateCheck", "")

    fun saveDomainsLastUpdatedDate(date: String = "") {
        prefs.put("domainsLastUpdated", date)
    }

    fun getDomainsLastUpdatedDate(): String = prefs.get("domainsLastUpdated", "")

    fun saveAiServicesLastUpdatedDate(date: String = "") {
        prefs.put("aiServicesLastUpdated", date)
    }

    fun getAiServicesLastUpdatedDate(): String = prefs.get("aiServicesLastUpdated", "")

    fun saveDomainsEtag(etag: String) { prefs.put("domainsEtag", etag) }
    fun getDomainsEtag(): String? = prefs.get("domainsEtag", null)
    fun saveAiServicesEtag(etag: String) { prefs.put("aiServicesEtag", etag) }
    fun getAiServicesEtag(): String? = prefs.get("aiServicesEtag", null)

    fun isOnboardingCompleted(): Boolean = prefs.getBoolean("onboarding", false)
    fun setOnboardingCompleted(completed: Boolean) {
        prefs.putBoolean("onboarding", completed)
    }

    fun getSettingVersion(): Int = prefs.getInt("settingVersion", 1)
}