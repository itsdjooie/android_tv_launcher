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
    onOpenFavorites: () -> Unit,
    onOpenHomeSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var plexHost by remember { mutableStateOf(configManager.plexHost) }
    var plexToken by remember { mutableStateOf(configManager.plexToken) }
    var roomName by remember { mutableStateOf(configManager.roomName) }
    var monitoringUrl by remember { mutableStateOf(configManager.monitoringApiUrl) }
    var monitoringKey by remember { mutableStateOf(configManager.monitoringApiKey) }

    var iptvType by remember { mutableStateOf(configManager.iptvType) }
    var iptvServer by remember { mutableStateOf(configManager.iptvServer) }
    var iptvUsername by remember { mutableStateOf(configManager.iptvUsername) }
    var iptvPassword by remember { mutableStateOf(configManager.iptvPassword) }
    var iptvM3uUrl by remember { mutableStateOf(configManager.iptvM3uUrl) }
    var iptvEpgUrl by remember { mutableStateOf(configManager.iptvEpgUrl) }
    var iptvAllowHttp by remember { mutableStateOf(configManager.iptvAllowHttp) }

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
                .width(680.dp)
                .height(600.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MlhkCardBg)
                .border(1.dp, MlhkCardBorderGlow, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "MLHK TV+ Launcher Instellingen",
                    style = Typography.titleLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MlhkTextMain)
                )

                // 1. Plex Section
                Text(
                    text = "PLEX MEDIA SERVER",
                    style = Typography.titleMedium.copy(color = MlhkNeonPink, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                )

                OutlinedTextField(
                    value = plexHost,
                    onValueChange = { plexHost = it },
                    label = { Text("Plex Server URL") },
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
                    label = { Text("X-Plex-Token") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MlhkTextMain,
                        unfocusedTextColor = MlhkTextSub,
                        focusedBorderColor = MlhkNeonPink,
                        unfocusedBorderColor = MlhkCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // 1-Click Plex PIN linking
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
                                    text = "Koppel direct met een 4-letterige code",
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
                                    .padding(vertical = 10.dp)
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
                                    Text(
                                        text = "Open op telefoon of pc: plex.tv/link",
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

                Spacer(modifier = Modifier.height(6.dp))

                // 2. IPTV Section
                Text(
                    text = "LIVE TV & IPTV INSTELLINGEN (ENKEL ZICHTBAAR NA KOPPELING)",
                    style = Typography.titleMedium.copy(color = MlhkNeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val types = listOf("none" to "Geen (Verborgen)", "xtream" to "Xtream Codes", "m3u" to "M3U Playlist")
                    types.forEach { (typeKey, typeLabel) ->
                        val isSelected = iptvType == typeKey
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MlhkNeonPink.copy(alpha = 0.25f) else MlhkSurface)
                                .border(1.dp, if (isSelected) MlhkNeonPink else MlhkCardBorder, RoundedCornerShape(10.dp))
                                .clickable { iptvType = typeKey }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = typeLabel,
                                style = Typography.bodySmall.copy(
                                    color = if (isSelected) MlhkTextMain else MlhkTextSub,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }

                if (iptvType == "xtream") {
                    OutlinedTextField(
                        value = iptvServer,
                        onValueChange = { iptvServer = it },
                        label = { Text("Xtream Server URL (bijv. http://provider.com:8080)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MlhkTextMain,
                            unfocusedTextColor = MlhkTextSub,
                            focusedBorderColor = MlhkNeonCyan,
                            unfocusedBorderColor = MlhkCardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = iptvUsername,
                            onValueChange = { iptvUsername = it },
                            label = { Text("Gebruikersnaam") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MlhkTextMain,
                                unfocusedTextColor = MlhkTextSub,
                                focusedBorderColor = MlhkNeonCyan,
                                unfocusedBorderColor = MlhkCardBorder
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = iptvPassword,
                            onValueChange = { iptvPassword = it },
                            label = { Text("Wachtwoord") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MlhkTextMain,
                                unfocusedTextColor = MlhkTextSub,
                                focusedBorderColor = MlhkNeonCyan,
                                unfocusedBorderColor = MlhkCardBorder
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else if (iptvType == "m3u") {
                    OutlinedTextField(
                        value = iptvM3uUrl,
                        onValueChange = { iptvM3uUrl = it },
                        label = { Text("M3U Playlist URL (HTTPS / HTTP)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MlhkTextMain,
                            unfocusedTextColor = MlhkTextSub,
                            focusedBorderColor = MlhkNeonCyan,
                            unfocusedBorderColor = MlhkCardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = iptvEpgUrl,
                        onValueChange = { iptvEpgUrl = it },
                        label = { Text("XMLTV Gids URL (Optioneel)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MlhkTextMain,
                            unfocusedTextColor = MlhkTextSub,
                            focusedBorderColor = MlhkNeonCyan,
                            unfocusedBorderColor = MlhkCardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (iptvType != "none") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { iptvAllowHttp = !iptvAllowHttp }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = if (iptvAllowHttp) "[✓] HTTP Streams toestaan" else "[ ] Alleen HTTPS streams",
                            style = Typography.bodySmall.copy(color = MlhkTextSub)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 3. Favoriete Apps & Launcher
                Text(
                    text = "APPS & LAUNCHER BEHEER",
                    style = Typography.titleMedium.copy(color = MlhkNeonPink, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                )

                OutlinedTextField(
                    value = roomName,
                    onValueChange = { roomName = it },
                    label = { Text("Kamer / Apparaatnaam (bijv. Woonkamer)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MlhkTextMain,
                        unfocusedTextColor = MlhkTextSub,
                        focusedBorderColor = MlhkNeonPink,
                        unfocusedBorderColor = MlhkCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TvButton(
                        text = "Favoriete Apps Kiezen",
                        isPrimary = false,
                        onClick = {
                            onOpenFavorites()
                            onDismiss()
                        }
                    )
                    TvButton(
                        text = "Standaard Launcher Instellen",
                        isPrimary = false,
                        onClick = onOpenHomeSettings
                    )
                }

                // 4. Monitoring Section
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "CENTRALE MONITORING STATUS API",
                    style = Typography.titleMedium.copy(color = MlhkNeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                )

                OutlinedTextField(
                    value = monitoringUrl,
                    onValueChange = { monitoringUrl = it },
                    label = { Text("Monitoring URL") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MlhkTextMain,
                        unfocusedTextColor = MlhkTextSub,
                        focusedBorderColor = MlhkNeonCyan,
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
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TvButton(
                        text = "Annuleren",
                        isPrimary = false,
                        onClick = {
                            isPollingPin = false
                            onDismiss()
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    TvButton(
                        text = "Opslaan & Toepassen",
                        isPrimary = true,
                        onClick = {
                            isPollingPin = false
                            configManager.plexHost = plexHost
                            configManager.plexToken = plexToken
                            configManager.roomName = roomName
                            configManager.monitoringApiUrl = monitoringUrl
                            configManager.monitoringApiKey = monitoringKey

                            configManager.iptvType = iptvType
                            configManager.iptvServer = iptvServer
                            configManager.iptvUsername = iptvUsername
                            configManager.iptvPassword = iptvPassword
                            configManager.iptvM3uUrl = iptvM3uUrl
                            configManager.iptvEpgUrl = iptvEpgUrl
                            configManager.iptvAllowHttp = iptvAllowHttp

                            onSaveAndReload()
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}
