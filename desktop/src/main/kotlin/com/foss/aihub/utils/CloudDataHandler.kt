package com.foss.aihub.pc.utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.call.body
import java.io.File

val httpClient = HttpClient(OkHttp) {
    install(HttpTimeout) {
        requestTimeoutMillis = 15_000L
        connectTimeoutMillis = 10_000L
        socketTimeoutMillis = 15_000L
    }
}

fun getEtag(url: String): String? {
    return try {
        val response = httpClient.head(url)
        response.headers["ETag"]
    } catch (_: Exception) {
        null
    }
}

fun fetchUrl(url: String, etag: String?): Pair<String, String?>? {
    val newEtag = getEtag(url)
    if (etag != null && newEtag == etag) return null
    return try {
        val response = httpClient.get(url)
        if (response.status.value == 200) {
            response.body<String>() to newEtag
        } else null
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