package com.foss.aihub.pc.models

import com.foss.aihub.pc.utils.generateAccentColorFromName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class RawAiService(
    val name: String,
    val website: String,
    val pricing: String,
    val privacy: String,
    val login_required: Boolean,
    val best_for: List<String>
)

data class AiService(
    val name: String,
    val url: String,
    val category: String,
    val pricing: String,
    val privacy: String,
    val loginRequired: Boolean,
    val bestFor: List<String>,
    val accentColor: Long
)

data class ModifiedServiceInfo(
    val service: AiService,
    val changes: List<String>
)

data class UpdateResult(
    val added: List<AiService>,
    val removed: List<AiService>,
    val modified: List<ModifiedServiceInfo>,
    val newCategories: Set<String>
)

data class AppSettings(
    var theme: String = "dark",
    var loadLastOpenedAI: Boolean = true,
    var multipleDefaultAi: Boolean = false,
    var defaultServiceName: String? = null,
    var defaultServiceNames: Set<String> = emptySet(),
    var serviceOrder: List<String> = emptyList(),
    var enabledServices: Set<String> = emptySet(),
    var favoriteServices: Set<String> = emptySet(),
    var maxKeepAlive: Int = 5,
    var enableZoom: Boolean = true,
    var desktopView: Boolean = true,
    var thirdPartyCookies: Boolean = false,
    var fontSizePercentage: Int = 100,
    var updateFrequencyDays: Int = 3,
    var blockAdsAndTrackers: Boolean = true,
    var checkForUpdate: Boolean = true,
    var isProxy: Boolean = false,
    var proxyType: String = "http",
    var proxyHost: String = "localhost",
    var proxyPort: String = "9050",
    var customCss: String = "",
    var customJs: String = "",
    var filterCategories: Set<String> = emptySet(),
    var filterPrices: Set<String> = emptySet(),
    var filterPrivacy: Set<String> = emptySet(),
    var filterLoginRequired: Boolean? = null,
    var enableNewServicesByDefault: Boolean = false,
    var preferredCategories: Set<String> = emptySet(),
    var preferredPrices: Set<String> = emptySet(),
    var preferredPrivacy: Set<String> = emptySet(),
    var preferredLoginRequired: Boolean? = null,
    var lastOpenedService: String? = null
)

val jsonFormat = Json { ignoreUnknownKeys = true; coerceInputValues = true }

fun loadServices(servicesFile: File): List<AiService> {
    if (!servicesFile.exists()) return emptyList()
    val jsonString = servicesFile.readText()
    val rawMap = jsonFormat.decodeFromString<Map<String, List<RawAiService>>>(jsonString)
    return rawMap.flatMap { (categoryName, rawServices) ->
        rawServices.map { raw ->
            AiService(
                name = raw.name,
                url = raw.website,
                category = categoryName,
                pricing = raw.pricing,
                privacy = raw.privacy,
                loginRequired = raw.login_required,
                bestFor = raw.best_for,
                accentColor = generateAccentColorFromName(raw.name)
            )
        }
    }
}

fun generateAccentColorFromName(name: String): Long {
    val hash = name.hashCode().toULong()
    val hue = (hash % 360u).toDouble()
    val lightness = 0.55 + ((hash / 360u) % 30u).toDouble() / 100.0
    return hslToColor(hue, lightness)
}

private fun hslToColor(hue: Double, lightness: Double): Long {
    val c = (1 - kotlin.math.abs(2 * lightness - 1)) * 0.7
    val x = c * (1 - kotlin.math.abs((hue / 60) % 2 - 1))
    val m = lightness - c / 2
    val (r, g, b) = when (hue.toInt()) {
        in 0..59 -> Triple(c, x, 0.0)
        in 60..119 -> Triple(x, c, 0.0)
        in 120..179 -> Triple(0.0, c, x)
        in 180..239 -> Triple(0.0, x, c)
        in 240..299 -> Triple(x, 0.0, c)
        else -> Triple(c, 0.0, x)
    }
    val rr = ((r + m) * 255).toInt().coerceIn(0, 255)
    val gg = ((g + m) * 255).toInt().coerceIn(0, 255)
    val bb = ((b + m) * 255).toInt().coerceIn(0, 255)
    return ((rr shl 16) or (gg shl 8) or bb).toLong()
}