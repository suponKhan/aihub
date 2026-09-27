package com.foss.aihub.pc

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.foss.aihub.pc.models.AiService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    selectedService: AiService?,
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(selectedService?.name ?: "AI Hub")
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Rounded.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Rounded.Settings, contentDescription = "Settings")
            }
            IconButton(onClick = onAboutClick) {
                Icon(Icons.Rounded.Info, contentDescription = "About")
            }
        }
    )
}