package nl.mlhk.tvlauncher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import nl.mlhk.tvlauncher.model.AppItem
import nl.mlhk.tvlauncher.ui.theme.*

@Composable
fun AppRow(
    title: String = "Mijn favoriete apps",
    apps: List<AppItem>,
    onAppClick: (AppItem) -> Unit,
    onManageFavorites: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (apps.isEmpty() && onManageFavorites == null) return

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 28.dp) // Ruime padding eronder tegen beeld-clipping
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
        ) {
            Text(
                text = title,
                style = Typography.titleLarge.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold, color = MlhkTextMain)
            )

            if (onManageFavorites != null) {
                ManageFavoritesPill(onClick = onManageFavorites)
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(apps, key = { it.packageName }) { app ->
                AppCardItem(app = app, onClick = { onAppClick(app) })
            }
        }
    }
}

@Composable
private fun ManageFavoritesPill(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isFocused) MlhkNeonPink.copy(alpha = 0.25f) else MlhkSurfaceHover)
            .border(1.dp, if (isFocused) MlhkNeonPink else MlhkCardBorder, RoundedCornerShape(12.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = "Favorieten beheren",
            tint = if (isFocused) MlhkNeonPink else MlhkTextSub,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "Favorieten aanpassen",
            style = Typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
                color = if (isFocused) MlhkTextMain else MlhkTextSub
            )
        )
    }
}

@Composable
private fun AppCardItem(
    app: AppItem,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.06f else 1.0f,
        label = "appScale"
    )

    val border = when {
        isFocused -> MlhkNeonPink
        else -> MlhkCardBorder
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(168.dp)
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
    ) {
        // 16:9 Landscape Card with clean border and background
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(168.dp)
                .height(100.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isFocused) MlhkSurfaceHover else MlhkCardBg)
                .border(if (isFocused) 2.dp else 1.dp, border, RoundedCornerShape(16.dp))
        ) {
            // If app provides a 16:9 Android TV Leanback Banner, render it full size!
            val bannerBitmap = remember(app.packageName) {
                try {
                    app.banner?.toBitmap(320, 180)?.asImageBitmap()
                } catch (_: Exception) {
                    null
                }
            }

            if (bannerBitmap != null) {
                Image(
                    bitmap = bannerBitmap,
                    contentDescription = app.label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (app.icon != null) {
                val iconBitmap = remember(app.packageName) {
                    try {
                        app.icon.toBitmap(96, 96).asImageBitmap()
                    } catch (_: Exception) {
                        null
                    }
                }
                if (iconBitmap != null) {
                    Image(
                        bitmap = iconBitmap,
                        contentDescription = app.label,
                        modifier = Modifier.size(54.dp)
                    )
                }
            } else {
                Text(
                    text = app.label.take(2).uppercase(),
                    style = Typography.titleMedium.copy(color = MlhkPrimary, fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = app.label,
            style = Typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Normal,
                color = if (isFocused) MlhkTextMain else MlhkTextSub
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
