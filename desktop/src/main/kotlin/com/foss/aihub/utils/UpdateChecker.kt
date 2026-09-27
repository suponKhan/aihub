package com.foss.aihub.pc

import java.net.URL
import java.net.HttpURLConnection
import java.io.BufferedReader
import java.io.InputStreamReader
import org.json.JSONObject

data class AppUpdateInfo(
    val versionName: String,
    val releaseNotes: String,
    val downloadUrl: String
)

suspend fun checkForUpdate(): AppUpdateInfo? {
    val url = "https://api.github.com/repos/$GITHUB_USER_NAME/$GITHUB_REPO_NAME/releases/latest"
    return try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        val response = connection.inputStream.bufferedReader().readText()
        val json = JSONObject(response)
        val latestVersion = json.getString("tag_name").removePrefix("v")
        val body = json.getString("body")
        val downloadUrl = json.optJSONArray("assets")?.takeIf { it.length() > 0 }
            ?.getJSONObject(0)?.getString("browser_download_url") ?: ""
        if (isNewerVersion(latestVersion, "1.0.0")) {
            AppUpdateInfo(latestVersion, body, downloadUrl)
        } else null
    } catch (_: Exception) {
        null
    }
}

private fun isNewerVersion(latest: String, current: String): Boolean {
    val latestParts = latest.split('.').map { it.toIntOrNull() ?: 0 }
    val currentParts = current.split('.').map { it.toIntOrNull() ?: 0 }
    for (i in 0 until maxOf(latestParts.size, currentParts.size)) {
        val l = latestParts.getOrElse(i) { 0 }
        val c = currentParts.getOrElse(i) { 0 }
        if (l != c) return l > c
    }
    return false
}