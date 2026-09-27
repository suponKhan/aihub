package com.foss.aihub.pc

import java.net.URL
import java.net.HttpURLConnection
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

fun openInExternalBrowser(url: String) {
    try {
        val os = Runtime.getRuntime().exec(arrayOf("cmd", "/c", "start", url))
        os.waitFor()
    } catch (_: Exception) {
        try {
            Runtime.getRuntime().exec(arrayOf("xdg-open", url))
        } catch (_: Exception) { }
    }
}

fun copyLinkToClipboard(url: String) {
    try {
        val escaped = url.replace("'", "''")
        val script = """
            |$escaped | Set-Clipboard
        """.trimMargin()
        Runtime.getRuntime().exec(arrayOf("powershell", "-Command", script)).waitFor()
    } catch (_: Exception) { }
}

fun shareLink(url: String, title: String) {
    val text = "$title\n$url"
    val encoded = URLEncoder.encode(text, StandardCharsets.UTF_8.name())
    openInExternalBrowser("https://www.reddit.com/submit?url=$encoded")
}

fun cleanTrackingParams(url: String): String {
    val trackingParams = listOf(
        "gclid", "fbclid", "msclkid", "ttclid", "twclid", "yclid",
        "igshid", "li_fat_id", "gbraid", "wbraid", "gad_source",
        "srsltid", "ndclid", "sccid", "dclid",
        "_ga", "_gl", "ef_id", "s_kwcid",
        "mc_cid", "mc_eid", "_bta_tid", "_bta_c",
        "trk_contact", "trk_msg", "_ke", "_kx", "dm_i", "mkt_tok",
        "ref", "affiliate_id", "click_id", "campid", "customid",
        "irclickid", "mkwid", "pcrid", "_branch_match_id",
        "gclsrc", "gdfms", "gdftrk", "epik", "pp", "si", "rtid", "vmcid",
        "_hsenc", "_hsmi", "__hssc", "__hstc",
    )
    return try {
        val uri = java.net.URI(url)
        val query = uri.query ?: return url
        val params = query.split("&").mapNotNull { kv ->
            val (k, v) = kv.split("=", limit = 2)
            if (k.lowercase() in trackingParams || k.lowercase().startsWith("utm_")) null else kv
        }
        val newQuery = params.joinToString("&")
        val newUri = java.net.URI(uri.scheme, uri.authority, uri.path, newQuery, uri.fragment)
        newUri.toString()
    } catch (_: Exception) {
        url
    }
}

fun extractLinkTitle(url: String): String {
    if (url.isEmpty()) return "Link"
    return try {
        url.substringAfterLast("/").substringBefore("?").substringBefore("#")
            .replace(Regex("[_-]"), " ").replace(Regex("\\.[a-zA-Z]{2,4}$"), "").trim()
            .takeIf { it.isNotEmpty() && it != "null" }?.split(" ")?.joinToString(" ") {
                it.replaceFirstChar { c -> c.uppercase() }
            } ?: "Link"
    } catch (_: Exception) {
        "Link"
    }
}

fun isDuplicateName(name: String, existingServices: List<AiService>): Boolean {
    val normalized = name.trim().lowercase()
    return existingServices.any { it.name.trim().lowercase() == normalized }
}

fun isDuplicateUrl(url: String, existingServices: List<AiService>): Boolean {
    val normalized = url.trim().lowercase().removePrefix("https://").removePrefix("http://").trimEnd('/')
    if (normalized.isBlank()) return false
    return existingServices.any { normalizeUrl(it.url) == normalized }
}

private fun normalizeUrl(url: String): String {
    return url.trim().lowercase().removePrefix("https://").removePrefix("http://").trimEnd('/')
}