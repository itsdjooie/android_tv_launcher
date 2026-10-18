package nl.mlhk.tvlauncher.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import nl.mlhk.tvlauncher.model.PlexMediaItem
import nl.mlhk.tvlauncher.ui.theme.*

@Composable
fun HeroBanner(
    item: PlexMediaItem?,
    onPlayClick: (PlexMediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var inWatchlist by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
    ) {
        // Backdrop Image with smooth crossfade
        Crossfade(targetState = item?.artUrl ?: item?.thumbUrl, label = "heroBackdrop") { url ->
            if (url != null) {
                AsyncImage(
                    model = url,
                    contentDescription = item?.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(MlhkSurfaceHover, MlhkBackground),
                                radius = 900f
                            )
                        )
                )
            }
        }

        // Multi-layered cinematic gradient vignettes
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MlhkBackground,
                            MlhkBackground.copy(alpha = 0.94f),
                            MlhkBackground.copy(alpha = 0.65f),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = 1350f
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MlhkBackground.copy(alpha = 0.75f),
                            MlhkBackground
                        ),
                        startY = 0f,
                        endY = 900f
                    )
                )
        )

        // Banner Content
        if (item != null) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxHeight()
                    .width(680.dp)
                    .padding(start = 48.dp, top = 20.dp, bottom = 18.dp)
            ) {
                // Category Tag
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = (item.showTitle ?: item.type).uppercase(),
                        style = Typography.labelMedium.copy(
                            color = MlhkNeonPink,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.3.sp
                        )
                    )
                    Text(
                        text = "• PLEX MEDIA",
                        style = Typography.bodySmall.copy(color = MlhkNeonCyan, fontSize = 11.sp)
                    )
                }

                // Main Title
                Text(
                    text = item.title,
                    style = Typography.displayLarge.copy(fontSize = 38.sp, fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // High-End Metadata Chips
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Rating Badge
                    val ratingValue = item.rating ?: 8.5f
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StatusOrange.copy(alpha = 0.2f))
                            .border(1.dp, StatusOrange.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "★ ${String.format("%.1f", ratingValue)}",
                            style = Typography.bodySmall.copy(
                                color = StatusOrange,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }

                    // 4K UHD Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MlhkSurfaceHover)
                            .border(1.dp, MlhkCardBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "4K HDR",
                            style = Typography.bodySmall.copy(
                                color = MlhkTextMain,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }

                    // Age / Content Rating
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MlhkSurfaceHover)
                            .border(1.dp, MlhkCardBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "16+",
                            style = Typography.bodySmall.copy(
                                color = MlhkTextSub,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        )
                    }

                    if (item.year != null) {
                        Text(
                            text = item.year.toString(),
                            style = Typography.bodySmall.copy(color = MlhkTextSub, fontSize = 13.sp)
                        )
                    }

                    if (item.displaySubtitle.isNotBlank() && item.showTitle == null) {
                        Text(
                            text = item.displaySubtitle,
                            style = Typography.bodySmall.copy(color = MlhkTextSub, fontSize = 13.sp)
                        )
                    }

                    if (item.genres.isNotEmpty()) {
                        Text(
                            text = item.genres.take(2).joinToString(" • "),
                            style = Typography.bodySmall.copy(color = MlhkTextMuted, fontSize = 13.sp)
                        )
                    }
                }

                // Synopsis
                Text(
                    text = item.summary ?: "Ervaar deze meeslepende titel in bioscoopkwaliteit via je Plex Media Server.",
                    style = Typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 21.sp),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.weight(1f))

                // Action CTA Buttons (KPN TV+ Replica)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeroActionButton(
                        text = if (item.progress > 0) "▶ Verder kijken" else "▶ Nu kijken",
                        isPrimary = true,
                        onClick = { onPlayClick(item) }
                    )

                    HeroActionButton(
                        text = if (inWatchlist) "✓ In mijn lijst" else "+ Mijn lijst",
                        isPrimary = false,
                        onClick = { inWatchlist = !inWatchlist }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroActionButton(
    text: String,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bg = when {
        isFocused -> if (isPrimary) MlhkNeonPinkHover else MlhkPrimary
        isPrimary -> MlhkPrimary
        else -> MlhkSurfaceHover
    }

    val textColor = when {
        isFocused -> if (isPrimary) MlhkBackground else MlhkBackground
        isPrimary -> MlhkBackground
        else -> MlhkTextMain
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(
                1.dp,
                if (isFocused) MlhkTextMain else (if (isPrimary) Color.Transparent else MlhkCardBorder),
                RoundedCornerShape(14.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 24.dp, vertical = 11.dp)
    ) {
        Text(
            text = text,
            style = Typography.titleMedium.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        )
    }
}
