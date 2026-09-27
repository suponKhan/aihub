package com.foss.aihub.pc

import java.io.File
import java.io.InputStream

fun Context.readAssetsFile(fileName: String): String {
    return try {
        val stream: InputStream = object {}.javaClass.classLoader
            .getResourceAsStream(fileName)
            ?: File(fileName).takeIf { it.exists() }?.inputStream()
            ?: return ""
        stream.bufferedReader().use { it.readText() }
    } catch (_: Exception) {
        ""
    }
}