package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppUpdateInfo
import kotlinx.coroutines.launch

@Composable
fun UpdatePromptDialog(
    updateInfo: AppUpdateInfo?,
    onDismiss: () -> Unit
) {
    if (updateInfo == null) return

    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    UpdatePromptContent(
        updateInfo = updateInfo,
        onDismiss = onDismiss,
        onUpdate = {
            scope.launch { com.example.utils.AppUpdater.openPlayStoreForUpdate(context) }
        }
    )
}

@Composable
internal fun UpdatePromptContent(updateInfo: AppUpdateInfo, onDismiss: () -> Unit, onUpdate: () -> Unit) {
    AdaptiveActionDialog(
        onDismiss = { if (!updateInfo.isMandatory) onDismiss() },
        dismissible = !updateInfo.isMandatory,
        modifier = Modifier.testTag("update_prompt_dialog"),
        actions = {
            Button(onClick = onUpdate, modifier = Modifier.fillMaxWidth().testTag("update_download_button")) {
                Text("Actualizar en Google Play", fontWeight = FontWeight.Bold)
            }
            if (!updateInfo.isMandatory) {
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().testTag("update_later_button")) { Text("Más tarde") }
            }
        }
    ) { bodyModifier ->
            Column(
                modifier = bodyModifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = "Actualizar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Hay una nueva versión",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "Versión ${updateInfo.versionName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = updateInfo.releaseNotes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

            }
    }
}
