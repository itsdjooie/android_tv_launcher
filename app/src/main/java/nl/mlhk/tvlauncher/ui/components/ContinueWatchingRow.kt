package nl.mlhk.tvlauncher.ui.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import coil.request.ImageRequest
import nl.mlhk.tvlauncher.model.ContinueWatchingItem
import nl.mlhk.tvlauncher.ui.theme.*

@Composable
fun ContinueWatchingRow(
    title: String = "Verder kijken",
    items: List<ContinueWatchingItem>,
    onItemClick: (ContinueWatchingItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Column(modifier = Modifier.padding(start = 48.dp)) {
            Text(
                text = "MLHK STREAMING // MULTI-SERVICE",
                style = Typography.labelSmall.copy(
                    color = MlhkNeonPink,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    style = Typography.titleLarge.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold, color = MlhkTextMain)
                )
                val sourcesText = items.map { it.sourceName }.distinct().joinToString(" • ", prefix = "• ")
                Text(
                    text = sourcesText,
                    style = Typography.bodySmall.copy(color = MlhkTextSub, fontSize = 12.sp)
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items, key = { it.id }) { item ->
                ContinueWatchingCard(item = item, onClick = { onItemClick(item) })
            }
        }
    }
}

@Composable
private fun ContinueWatchingCard(
    item: ContinueWatchingItem,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.06f else 1.0f,
        label = "cwScale"
    )

    val border = when {
        isFocused -> MlhkNeonPink
        else -> MlhkCardBorder
    }

    val brandColor = when (item.sourceName.lowercase()) {
        "plex" -> Color(0xFFE5A00D)
        "videoland" -> Color(0xFFE5003A)
        "prime video", "prime", "amazon prime" -> Color(0xFF00A8E1)
        "npo start", "npo" -> Color(0xFFFF5900)
        "vtm go", "vtm" -> Color(0xFFFF0055)
        "spotify" -> Color(0xFF1ED760)
        "hbo max" -> Color(0xFF9E47FF)
        "disney+" -> Color(0xFF0072D2)
        "netflix" -> Color(0xFFE50914)
        "youtube" -> Color(0xFFFF0000)
        "viaplay" -> Color(0xFFFF1B51)
        "kodi" -> Color(0xFF17B2E7)
        "smarters iptv" -> MlhkNeonCyan
        else -> MlhkPrimary
    }

    val context = LocalContext.current
    val bannerBitmap = remember(item.id, item.appBanner) {
        try {
            item.appBanner?.toBitmap(480, 270)?.asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }
    val iconBitmap = remember(item.id, item.appIcon) {
        try {
            item.appIcon?.toBitmap(128, 128)?.asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }

    Column(
        modifier = Modifier
            .width(240.dp)
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
    ) {
        // Thumbnail Box
        Box(
            modifier = Modifier
                .width(240.dp)
                .height(135.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MlhkCardBg)
                .border(if (isFocused) 2.dp else 1.dp, border, RoundedCornerShape(16.dp))
        ) {
            if (!item.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(item.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (bannerBitmap != null) {
                Image(
                    bitmap = bannerBitmap,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (iconBitmap != null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.radialGradient(
                                listOf(brandColor.copy(alpha = 0.35f), MlhkCardBg)
                            )
                        )
                ) {
                    Image(
                        bitmap = iconBitmap,
                        contentDescription = item.title,
                        modifier = Modifier.size(56.dp)
                    )
                }
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.radialGradient(
                                listOf(brandColor.copy(alpha = 0.35f), MlhkCardBg)
                            )
                        )
                ) {
                    Text(
                        text = item.sourceName.uppercase(),
                        style = Typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = brandColor
                        )
                    )
                }
            }

            // Subtle dark bottom gradient for high contrast readability of progress bar
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.65f)
                            )
                        )
                    )
            )

            // Top Badges: Service Badge + Remaining Time
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // App Badge Pill with exact MLHK glass design
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.85f))
                        .border(0.5.dp, brandColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = item.sourceName.uppercase(),
                        style = Typography.bodySmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = brandColor
                        )
                    )
                }

                if (item.remainingTime != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.85f))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.remainingTime,
                            style = Typography.bodySmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            // Progress bar at the bottom
            if (item.progress > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Color.Black.copy(alpha = 0.7f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(item.progress)
                            .background(if (isFocused) MlhkNeonPink else brandColor)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = item.title,
            style = Typography.titleMedium.copy(
                fontSize = 13.sp,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
                color = if (isFocused) MlhkTextMain else MlhkTextSub
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Subtitle
        Text(
            text = item.subtitle,
            style = Typography.bodySmall.copy(
                fontSize = 11.sp,
                color = MlhkTextMuted
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
