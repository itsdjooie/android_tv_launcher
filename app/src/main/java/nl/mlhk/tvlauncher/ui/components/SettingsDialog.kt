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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.mlhk.tvlauncher.data.ConfigManager
import nl.mlhk.tvlauncher.data.PlexClient
import nl.mlhk.tvlauncher.data.PlexPinInfo
import nl.mlhk.tvlauncher.ui.theme.*

@Composable
fun SettingsDialog(
    configManager: ConfigManager,
    plexClient: PlexClient,
    onSaveAndReload: () -> Unit,
    onReturnToKpn: () -> Unit,
    onOpenHomeSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var plexHost by remember { mutableStateOf(configManager.plexHost) }
    var plexToken by remember { mutableStateOf(configManager.plexToken) }
    var monitoringUrl by remember { mutableStateOf(configManager.monitoringApiUrl) }
    var monitoringKey by remember { mutableStateOf(configManager.monitoringApiKey) }

    var pinInfo by remember { mutableStateOf<PlexPinInfo?>(null) }
    var isPollingPin by remember { mutableStateOf(false) }
    var pinStatusMessage by remember { mutableStateOf<String?>(null) }

    fun startPlexPinFlow() {
        coroutineScope.launch {
            pinStatusMessage = "PIN aanvragen bij Plex.tv..."
            val pin = plexClient.createPin()
            if (pin != null) {
                pinInfo = pin
                isPollingPin = true
                pinStatusMessage = "Ga op je mobiel of pc naar https://plex.tv/link en voer code in."

                // Poll every 2 seconds for 10 minutes
                while (isPollingPin) {
                    delay(2000)
                    val token = plexClient.pollPin(pin.id)
                    if (!token.isNullOrBlank()) {
                        plexToken = token
                        configManager.plexToken = token
                        configManager.plexHost = plexHost
                        isPollingPin = false
                        pinStatusMessage = "✓ Succesvol gekoppeld met Plex!"
                        onSaveAndReload()
                        break
                    }
                }
            } else {
                pinStatusMessage = "Kon geen PIN ophalen. Controleer internetverbinding."
            }
        }
    }

    Dialog(onDismissRequest = {
        isPollingPin = false
        onDismiss()
    }) {
        Box(
            modifier = Modifier
                .width(620.dp)
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
                    style = Typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold)
                )

                // Plex Section
                Text(
                    text = "Plex Media Server koppeling",
                    style = Typography.titleMedium.copy(color = MlhkNeonCyan, fontSize = 14.sp)
                )

                OutlinedTextField(
                    value = plexHost,
                    onValueChange = { plexHost = it },
                    label = { Text("Plex Server URL (HTTPS ondersteund)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MlhkTextMain,
                        unfocusedTextColor = MlhkTextSub,
                        focusedBorderColor = MlhkNeonPink,
                        unfocusedBorderColor = MlhkCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // 1-Click Plex PIN linking box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MlhkSurface)
                        .border(1.dp, if (pinInfo != null) MlhkNeonPink else MlhkCardBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Snelle koppeling via Plex.tv/link",
                                    style = Typography.titleMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "Koppel direct zonder lang token over te typen",
                                    style = Typography.bodySmall.copy(color = MlhkTextMuted)
                                )
                            }
                            TvButton(
                                text = if (pinInfo != null) "Nieuwe Code" else "Koppel via PIN",
                                isPrimary = true,
                                onClick = { startPlexPinFlow() }
                            )
                        }

                        if (pinInfo != null) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MlhkCardBg)
                                    .padding(vertical = 12.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = pinInfo!!.code.uppercase(),
                                        style = Typography.displayLarge.copy(
                                            color = MlhkNeonPink,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 4.sp,
                                            fontSize = 28.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Open op telefoon/pc: plex.tv/link",
                                        style = Typography.bodySmall.copy(color = MlhkTextMain, fontWeight = FontWeight.Medium)
                                    )
                                }
                            }
                        }

                        if (pinStatusMessage != null) {
                            Text(
                                text = pinStatusMessage!!,
                                style = Typography.bodySmall.copy(
                                    color = if (pinStatusMessage!!.startsWith("✓")) StatusGreen else MlhkNeonCyan,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = plexToken,
                    onValueChange = { plexToken = it },
                    label = { Text("Of handmatig: X-Plex-Token") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MlhkTextMain,
                        unfocusedTextColor = MlhkTextSub,
                        focusedBorderColor = MlhkNeonPink,
                        unfocusedBorderColor = MlhkCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Monitoring Section
                Text(
                    text = "Centrale Monitoring-API",
                    style = Typography.titleMedium.copy(color = MlhkNeonCyan, fontSize = 14.sp)
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

                // Actions
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
                            isPollingPin = false
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
