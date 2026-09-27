package com.foss.aihub.pc

import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeParseException

class SettingsBackupHelper(private val settingsManager: SettingsManager) {
    fun backup(): String {
        val backup = mapOf(
            "version" to 1,
            "timestamp" to LocalDate.now().toString(),
            "settings" to settingsManager.settings
        )
        return com.google.gson.Gson().newBuilder().setPrettyPrinting().create().toJson(backup)
    }

    fun restore(json: String): Boolean {
        return try {
            val obj = org.json.JSONObject(json)
            val settings = obj.optJSONObject("settings") ?: return false
            settingsManager.settings = AppSettings(
                theme = settings.getString("theme"),
                loadLastOpenedAI = settings.getBoolean("loadLastOpenedAI"),
                multipleDefaultAi = settings.getBoolean("multipleDefaultAi"),
                defaultServiceName = settings.getString("defaultServiceName"),
                defaultServiceNames = settings.optJSONArray("defaultServiceNames")?.let {
                    (0 until it.length()).map { it.getString(it.getInt(it)) }.toSet()
                } ?: emptySet(),
                serviceOrder = settings.optJSONArray("serviceOrder")?.let {
                    (0 until it.length()).map { it.getString(it.getInt(it)) }.toList()
                } ?: emptyList(),
                enabledServices = settings.optJSONArray("enabledServices")?.let {
                    (0 until it.length()).map { it.getString(it.getInt(it)) }.toSet()
                } ?: emptySet(),
                favoriteServices = settings.optJSONArray("favoriteServices")?.let {
                    (0 until it.length()).map { it.getString(it.getInt(it)) }.toSet()
                } ?: emptySet(),
                maxKeepAlive = settings.getInt("maxKeepAlive"),
                enableZoom = settings.getBoolean("enableZoom"),
                desktopView = settings.getBoolean("desktopView"),
                thirdPartyCookies = settings.getBoolean("thirdPartyCookies"),
                fontSizePercentage = settings.getInt("fontSizePercentage"),
                updateFrequencyDays = settings.getInt("updateFrequencyDays"),
                blockAdsAndTrackers = settings.getBoolean("blockAdsAndTrackers"),
                checkForUpdate = settings.getBoolean("checkForUpdate"),
                isProxy = settings.getBoolean("isProxy"),
                proxyType = settings.getString("proxyType"),
                proxyHost = settings.getString("proxyHost"),
                proxyPort = settings.getString("proxyPort"),
                customCss = settings.getString("customCss"),
                customJs = settings.getString("customJs"),
                filterCategories = settings.optJSONArray("filterCategories")?.let {
                    (0 until it.length()).map { it.getString(it.getInt(it)) }.toSet()
                } ?: emptySet(),
                filterPrices = settings.optJSONArray("filterPrices")?.let {
                    (0 until it.length()).map { it.getString(it.getInt(it)) }.toSet()
                } ?: emptySet(),
                filterPrivacy = settings.optJSONArray("filterPrivacy")?.let {
                    (0 until it.length()).map { it.getString(it.getInt(it)) }.toSet()
                } ?: emptySet(),
                filterLoginRequired = settings.optJSONObject("filterLoginRequired")?.let {
                    it.getBoolean("filterLoginRequired")
                },
                enableNewServicesByDefault = settings.getBoolean("enableNewServicesByDefault"),
                preferredCategories = settings.optJSONArray("preferredCategories")?.let {
                    (0 until it.length()).map { it.getString(it.getInt(it)) }.toSet()
                } ?: emptySet(),
                preferredPrices = settings.optJSONArray("preferredPrices")?.let {
                    (0 until it.length()).map { it.getString(it.getInt(it)) }.toSet()
                } ?: emptySet(),
                preferredPrivacy = settings.optJSONArray("preferredPrivacy")?.let {
                    (0 until it.length()).map { it.getString(it.getInt(it)) }.toSet()
                } ?: emptySet(),
                preferredLoginRequired = settings.optJSONObject("preferredLoginRequired")?.let {
                    it.getBoolean("preferredLoginRequired")
                }
            )
            true
        } catch (_: Exception) {
            false
        }
    }
}