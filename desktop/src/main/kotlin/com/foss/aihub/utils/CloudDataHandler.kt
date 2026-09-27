package com.foss.aihub.pc.utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.util.network.UnresolvedAddressException
import java.io.File
import java.net.ConnectException
import java.net.SocketException
import java.net.UnknownHostException

val httpClient = HttpClient(OkHttp) {
    install(HttpTimeout) {
        requestTimeoutMillis = 15_000L
        connectTimeoutMillis = 10_000L
        socketTimeoutMillis = 15_000L
    }
}

suspend fun getEtag(url: String): String? {
    return try {
        val response = httpClient.head(url)
        response.headers["ETag"]
    } catch (_: Exception) {
        null
    }
}

suspend fun fetchUrl(url: String, etag: String?): Pair<String, String?>? {
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
    val response = io.ktor.client.runBlocking {
        fetchUrl(CLOUD_BASE_URL + AI_SERVICES_FILE, etag)
    } ?: return null
    val (body, newEtag) = response
    file.writeText(body)
    return newEtag
}

fun updateDomains(file: File, etag: String?): String? {
    val response = io.ktor.client.runBlocking {
        fetchUrl(CLOUD_BASE_URL + DOMAINS_FILE, etag)
    } ?: return null
    val (body, newEtag) = response
    file.writeText(body)
    return newEtag
}

fun Throwable.isNoNetworkError(): Boolean {
    val root = rootCause()
    return root is UnresolvedAddressException || root is UnknownHostException ||
            root is ConnectException || (root is SocketException &&
            root.message?.contains("unreachable", ignoreCase = true) == true) ||
            root.message?.contains("Network is unreachable", ignoreCase = true) == true ||
            root.message?.contains("unresolved address", ignoreCase = true) == true
}

private fun Throwable.rootCause(): Throwable {
    var cause = this
    while (cause.cause != null && cause.cause !== cause) {
        cause = cause.cause!!
    }
    return cause
}