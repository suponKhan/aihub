package com.foss.aihub.pc

import com.foss.aihub.pc.utils.CLOUD_BASE_URL
import com.foss.aihub.pc.utils.AI_SERVICES_FILE
import com.foss.aihub.pc.utils.DOMAINS_FILE
import com.foss.aihub.pc.utils.jsonFormat
import com.foss.aihub.pc.models.RawAiService
import com.foss.aihub.pc.utils.httpClient
import io.ktor.client.request.get
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.util.network.UnresolvedAddressException
import java.net.ConnectException
import java.net.SocketException
import java.net.UnknownHostException

object CloudDataHandler {
    suspend fun updateAiServices(file: File, etag: String?): String? {
        return try {
            val response = httpClient.get(CLOUD_BASE_URL + AI_SERVICES_FILE)
            if (response.status.value == 200) {
                val body = response.body<String>()
                val newEtag = response.headers["ETag"]
                file.writeText(body)
                newEtag
            } else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun updateDomains(file: File, etag: String?): String? {
        return try {
            val response = httpClient.get(CLOUD_BASE_URL + DOMAINS_FILE)
            if (response.status.value == 200) {
                val body = response.body<String>()
                val newEtag = response.headers["ETag"]
                file.writeText(body)
                newEtag
            } else null
        } catch (_: Exception) {
            null
        }
    }
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