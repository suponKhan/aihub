package com.foss.aihub.pc.utils

import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Scanner

fun getEtag(url: String): String? {
    return try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "HEAD"
        connection.connectTimeout = 10000
        connection.readTimeout = 15000
        val etag = connection.getHeaderField("ETag")
        connection.disconnect()
        etag
    } catch (_: Exception) {
        null
    }
}

fun fetchUrl(url: String, etag: String?): Pair<String, String?>? {
    val newEtag = getEtag(url)
    if (etag != null && newEtag == etag) return null
    return try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10000
        connection.readTimeout = 15000
        if (connection.responseCode == 200) {
            val body = Scanner(connection.getInputStream()).useDelimiter("\\A").next()
            connection.disconnect()
            body to newEtag
        } else {
            connection.disconnect()
            null
        }
    } catch (_: Exception) {
        null
    }
}

fun updateAiServices(file: File, etag: String?): String? {
    val response = fetchUrl(CLOUD_BASE_URL + AI_SERVICES_FILE, etag) ?: return null
    val (body, newEtag) = response
    file.writeText(body)
    return newEtag
}

fun updateDomains(file: File, etag: String?): String? {
    val response = fetchUrl(CLOUD_BASE_URL + DOMAINS_FILE, etag) ?: return null
    val (body, newEtag) = response
    file.writeText(body)
    return newEtag
}