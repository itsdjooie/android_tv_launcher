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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
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
import nl.mlhk.tvlauncher.data.ConfigManager
import nl.mlhk.tvlauncher.model.AppItem
import nl.mlhk.tvlauncher.ui.theme.*

@Composable
fun FavoritesDialog(
    allApps: List<AppItem>,
    configManager: ConfigManager,
    onFavoritesUpdated: () -> Unit,
    onDismiss: () -> Unit
) {
    var favoriteSet by remember { mutableStateOf(configManager.favoritePackages.toMutableSet()) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(600.dp)
                .height(520.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MlhkCardBg)
                .border(1.dp, MlhkCardBorderGlow, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Favoriete Apps Instellen",
                            style = Typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MlhkTextMain)
                        )
                        Text(
                            text = "Selecteer welke apps in 'Mijn favoriete apps' worden getoond",
                            style = Typography.bodySmall.copy(color = MlhkTextSub)
                        )
                    }

                    TvButton(
                        text = "Klaar",
                        isPrimary = true,
                        onClick = {
                            configManager.setFavorites(favoriteSet)
                            onFavoritesUpdated()
                            onDismiss()
                        }
                    )
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(allApps, key = { it.packageName }) { app ->
                        val isFav = favoriteSet.contains(app.packageName)
                        FavoriteAppItemRow(
                            app = app,
                            isFavorite = isFav,
                            onToggle = {
                                val next = favoriteSet.toMutableSet()
                                if (isFav) {
                                    next.remove(app.packageName)
                                } else {
                                    next.add(app.packageName)
                                }
                                favoriteSet = next
                                configManager.setFavorites(next)
                                onFavoritesUpdated()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteAppItemRow(
    app: AppItem,
    isFavorite: Boolean,
    onToggle: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isFocused) MlhkSurfaceHover else Color.Transparent)
            .border(1.dp, if (isFocused) MlhkNeonPink else MlhkCardBorder.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onToggle)
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = null,
                tint = if (isFavorite) MlhkNeonPink else MlhkTextSub,
                modifier = Modifier.size(22.dp)
            )

            Column {
                Text(
                    text = app.label,
                    style = Typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
                        color = MlhkTextMain
                    )
                )
                Text(
                    text = app.packageName,
                    style = Typography.labelSmall.copy(fontSize = 11.sp, color = MlhkTextSub)
                )
            }
        }

        if (isFavorite) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MlhkNeonPink.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Favoriet",
                    style = Typography.labelSmall.copy(color = MlhkNeonPink, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
