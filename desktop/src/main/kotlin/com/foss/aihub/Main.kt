package com.foss.aihub.pc

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.foss.aihub.pc.models.AppSettings
import com.foss.aihub.pc.models.AiService
import com.foss.aihub.pc.models.loadServices
import com.foss.aihub.pc.utils.SettingsManager
import java.io.File

lateinit var settingsManager: SettingsManager
lateinit var dataDir: File
lateinit var servicesFile: File
lateinit var domainsFile: File
var aiServices by mutableStateOf<List<AiService>>(emptyList())
    private set
var settings by mutableStateOf(AppSettings())
    private set

fun updateSettings(new: AppSettings) {
    settings = new
    settingsManager.settings = new
}

fun updateServices(list: List<AiService>) {
    aiServices = list
}

fun saveSettings() {
    settingsManager.settings = settings
}

fun refreshServices() {
    aiServices = loadServices(servicesFile)
}

fun main() = application {
    val userHome = System.getProperty("user.home")
    dataDir = File(userHome, ".aihub-pc").apply { mkdirs() }
    servicesFile = File(dataDir, "ais.json")
    domainsFile = File(dataDir, "domains.txt")
    settingsManager = SettingsManager(dataDir)
    settings = settingsManager.settings
    refreshServices()

    Window(
        title = "AI Hub",
        state = rememberWindowState(),
        onCloseRequest = ::exitApplication
    ) {
        App()
    }
}