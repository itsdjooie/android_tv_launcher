package nl.mlhk.tvlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import nl.mlhk.tvlauncher.data.ConfigManager
import nl.mlhk.tvlauncher.ui.theme.*

@Composable
fun SettingsDialog(
    configManager: ConfigManager,
    onSaveAndReload: () -> Unit,
    onReturnToKpn: () -> Unit,
    onOpenHomeSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    var plexHost by remember { mutableStateOf(configManager.plexHost) }
    var plexToken by remember { mutableStateOf(configManager.plexToken) }
    var monitoringUrl by remember { mutableStateOf(configManager.monitoringApiUrl) }
    var monitoringKey by remember { mutableStateOf(configManager.monitoringApiKey) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(580.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MlhkCardBg)
                .border(1.dp, MlhkCardBorder, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "MLHK TV+ Launcher Instellingen",
                    style = Typography.titleLarge
                )

                Text(
                    text = "Plex Media Server koppeling",
                    style = Typography.titleMedium.copy(color = MlhkNeonCyan, fontSize = 13.sp)
                )

                OutlinedTextField(
                    value = plexHost,
                    onValueChange = { plexHost = it },
                    label = { Text("Plex Server URL (bijv. http://192.168.2.25:32400)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MlhkTextMain,
                        unfocusedTextColor = MlhkTextSub,
                        focusedBorderColor = MlhkNeonPink,
                        unfocusedBorderColor = MlhkCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = plexToken,
                    onValueChange = { plexToken = it },
                    label = { Text("Plex Token (X-Plex-Token)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MlhkTextMain,
                        unfocusedTextColor = MlhkTextSub,
                        focusedBorderColor = MlhkNeonPink,
                        unfocusedBorderColor = MlhkCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Centrale Monitoring-API",
                    style = Typography.titleMedium.copy(color = MlhkNeonCyan, fontSize = 13.sp)
                )

                OutlinedTextField(
                    value = monitoringUrl,
                    onValueChange = { monitoringUrl = it },
                    label = { Text("Monitoring API URL (bijv. http://192.168.2.25:8080/api/status)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MlhkTextMain,
                        unfocusedTextColor = MlhkTextSub,
                        focusedBorderColor = MlhkNeonPink,
                        unfocusedBorderColor = MlhkCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = monitoringKey,
                    onValueChange = { monitoringKey = it },
                    label = { Text("Monitoring API Token / Key") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MlhkTextMain,
                        unfocusedTextColor = MlhkTextSub,
                        focusedBorderColor = MlhkNeonPink,
                        unfocusedBorderColor = MlhkCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MlhkCardBorder)
                )

                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TvButton(
                        text = "Terug naar KPN",
                        isPrimary = false,
                        onClick = onReturnToKpn
                    )
                    TvButton(
                        text = "Standaard Launcher Wijzigen",
                        isPrimary = false,
                        onClick = onOpenHomeSettings
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TvButton(
                        text = "Opslaan",
                        isPrimary = true,
                        onClick = {
                            configManager.plexHost = plexHost
                            configManager.plexToken = plexToken
                            configManager.monitoringApiUrl = monitoringUrl
                            configManager.monitoringApiKey = monitoringKey
                            onSaveAndReload()
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TvButton(
    text: String,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bg = when {
        isFocused -> if (isPrimary) MlhkNeonPinkHover else MlhkSurfaceHover
        isPrimary -> MlhkPrimary
        else -> MlhkSurface
    }

    val border = when {
        isFocused -> MlhkNeonPink
        else -> MlhkCardBorder
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(
            text = text,
            style = Typography.labelMedium.copy(
                color = if (isPrimary) MlhkBackground else MlhkTextMain
            )
        )
    }
}
