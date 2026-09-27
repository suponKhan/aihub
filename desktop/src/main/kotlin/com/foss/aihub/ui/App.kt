package com.foss.aihub.pc

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.foss.aihub.pc.models.AiService
import com.foss.aihub.pc.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val settings by derivedStateOf { settingsManager.settings }
    val initialServiceName = if (settings.loadLastOpenedAI) {
        settingsManager.getLastOpenedService() ?: settings.defaultServiceName ?: aiServices.firstOrNull()?.name
    } else {
        settings.defaultServiceName ?: aiServices.firstOrNull()?.name
    } ?: ""

    var selectedService by remember {
        mutableStateOf(aiServices.find { it.name == initialServiceName } ?: aiServices.firstOrNull())
    }

    LaunchedEffect(aiServices) {
        if (aiServices.isNotEmpty() && selectedService == null) {
            selectedService = aiServices.first()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            DrawerContent(
                aiServices = aiServices,
                selectedService = selectedService,
                onServiceSelected = { service ->
                    selectedService = service
                    scope.launch { drawerState.close() }
                },
                settingsManager = settingsManager
            )
        }
    ) {
        Scaffold(
            topBar = {
                AppBar(
                    selectedService = selectedService,
                    onMenuClick = {
                        scope.launch {
                            if (drawerState.isOpen) drawerState.close() else drawerState.open()
                        }
                    },
                    onSettingsClick = { /* TODO */ },
                    onAboutClick = { /* TODO */ }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                selectedService?.let { service ->
                    ServiceView(service = service)
                }
            }
        }
    }
}

@Composable
fun ServiceView(service: AiService) {
    val webState = rememberWebViewState()
    var url by remember { mutableStateOf(service.url) }

    LaunchedEffect(service) {
        url = service.url
    }

    Box(modifier = Modifier.fillMaxSize()) {
        WebView(
            state = webState,
            modifier = Modifier.fillMaxSize()
        )
    }
}