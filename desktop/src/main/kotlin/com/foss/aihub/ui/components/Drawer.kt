package com.foss.aihub.pc

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.foss.aihub.pc.models.AiService
import com.foss.aihub.pc.utils.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerContent(
    aiServices: List<AiService>,
    selectedService: AiService?,
    onServiceSelected: (AiService) -> Unit,
    settingsManager: SettingsManager
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("AI Hub", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(aiServices) { service ->
                ServiceListItem(
                    service = service,
                    isSelected = service == selectedService,
                    onClick = { onServiceSelected(service) }
                )
            }
        }
    }
}

@Composable
fun ServiceListItem(
    service: AiService,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = service.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = service.category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}