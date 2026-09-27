package com.foss.aihub.pc

import java.io.File
import java.io.InputStream

fun readResourceFile(fileName: String): String {
    return try {
        val stream: InputStream = object {}.javaClass.classLoader
            .getResourceAsStream(fileName)
            ?: return ""
        stream.bufferedReader().use { it.readText() }
    } catch (_: Exception) {
        ""
    }
}

fun String.capitalizeFirstLetter(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }