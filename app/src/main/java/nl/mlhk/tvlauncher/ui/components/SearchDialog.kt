package nl.mlhk.tvlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import nl.mlhk.tvlauncher.model.AppItem
import nl.mlhk.tvlauncher.model.PlexMediaItem
import nl.mlhk.tvlauncher.ui.theme.*

@Composable
fun SearchDialog(
    plexItems: List<PlexMediaItem>,
    apps: List<AppItem>,
    onSelectPlex: (PlexMediaItem) -> Unit,
    onSelectApp: (AppItem) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    val filteredPlex = remember(query, plexItems) {
        if (query.isBlank()) emptyList()
        else plexItems.filter { it.title.contains(query, ignoreCase = true) || it.showTitle?.contains(query, ignoreCase = true) == true }
    }

    val filteredApps = remember(query, apps) {
        if (query.isBlank()) emptyList()
        else apps.filter { it.label.contains(query, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(640.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MlhkCardBg)
                .border(1.dp, MlhkCardBorder, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Zoeken in Plex en Apps",
                    style = Typography.titleLarge
                )

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Typ een titel, serie of app...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MlhkTextMain,
                        unfocusedTextColor = MlhkTextSub,
                        focusedBorderColor = MlhkNeonPink,
                        unfocusedBorderColor = MlhkCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Results list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    if (query.isBlank()) {
                        item {
                            Text(
                                text = "Zoek direct in je Plex-bibliotheek en geïnstalleerde televisie-apps.",
                                style = Typography.bodyMedium.copy(color = MlhkTextMuted),
                                modifier = Modifier.padding(vertical = 20.dp)
                            )
                        }
                    } else if (filteredPlex.isEmpty() && filteredApps.isEmpty()) {
                        item {
                            Text(
                                text = "Geen resultaten gevonden voor '$query'.",
                                style = Typography.bodyMedium.copy(color = MlhkTextMuted),
                                modifier = Modifier.padding(vertical = 20.dp)
                            )
                        }
                    } else {
                        items(filteredPlex) { item ->
                            SearchResultItem(
                                title = item.title,
                                subtitle = "Plex • ${item.displaySubtitle}",
                                tag = item.type.uppercase(),
                                onClick = { onSelectPlex(item) }
                            )
                        }
                        items(filteredApps) { app ->
                            SearchResultItem(
                                title = app.label,
                                subtitle = "Applicatie",
                                tag = "APP",
                                onClick = { onSelectApp(app) }
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TvButton(text = "Sluiten", isPrimary = false, onClick = onDismiss)
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    title: String,
    subtitle: String,
    tag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isFocused) MlhkSurfaceHover else MlhkSurface)
            .border(1.dp, if (isFocused) MlhkNeonPink else Color.Transparent, RoundedCornerShape(10.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column {
            Text(text = title, style = Typography.titleMedium.copy(fontSize = 14.sp))
            Text(text = subtitle, style = Typography.bodySmall.copy(color = MlhkTextMuted))
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isFocused) MlhkPrimary else MlhkCardBg)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = tag,
                style = Typography.bodySmall.copy(
                    fontSize = 10.sp,
                    color = if (isFocused) MlhkBackground else MlhkNeonPink
                )
            )
        }
    }
}
