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
import kotlinx.coroutines.delay
import nl.mlhk.tvlauncher.model.PlexMediaItem
import nl.mlhk.tvlauncher.ui.theme.*

@Composable
fun HeroBanner(
    items: List<PlexMediaItem>,
    onPlayClick: (PlexMediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var inWatchlist by remember { mutableStateOf(false) }

    val highlights = remember(items) { items.take(5) }
    var currentIndex by remember { mutableStateOf(0) }

    // Zorg dat de index binnen bereik blijft
    val validIndex = if (highlights.isNotEmpty()) currentIndex.coerceIn(0, highlights.lastIndex) else 0
    val currentItem = highlights.getOrNull(validIndex)

    // Automatische rustige rotatie door de 3-5 highlights (elke 9 seconden)
    LaunchedEffect(highlights) {
        if (highlights.size > 1) {
            while (true) {
                delay(9000)
                if (highlights.isNotEmpty()) {
                    currentIndex = (currentIndex + 1) % highlights.size
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
    ) {
        // Backdrop Image met soepele crossfade (direct uit cache, 0ms laadtijd)
        Crossfade(targetState = currentItem?.artUrl ?: currentItem?.thumbUrl, label = "heroBackdrop") { url ->
            if (url != null) {
                AsyncImage(
                    model = url,
                    contentDescription = currentItem?.title,
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

        // Multi-layered cinematische vignetgradiënten
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

        // Banner Inhoud
        if (currentItem != null) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxHeight()
                    .width(680.dp)
                    .padding(start = 48.dp, top = 20.dp, bottom = 18.dp)
            ) {
                // Category Tag met Highlight-nummering
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (highlights.size > 1) {
                            "HIGHLIGHT ${validIndex + 1} VAN ${highlights.size}"
                        } else {
                            (currentItem.showTitle ?: currentItem.type).uppercase()
                        },
                        style = Typography.labelMedium.copy(
                            color = MlhkNeonPink,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.3.sp
                        )
                    )
                    Text(
                        text = "• PLEX SPOTLIGHT",
                        style = Typography.bodySmall.copy(color = MlhkNeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    )
                }

                // Hoofdtitel
                Text(
                    text = currentItem.title,
                    style = Typography.displayLarge.copy(fontSize = 38.sp, fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Metadata Chips
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val ratingValue = currentItem.rating ?: 8.5f
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

                    if (currentItem.year != null) {
                        Text(
                            text = currentItem.year.toString(),
                            style = Typography.bodySmall.copy(color = MlhkTextSub, fontSize = 13.sp)
                        )
                    }

                    if (currentItem.displaySubtitle.isNotBlank() && currentItem.showTitle == null) {
                        Text(
                            text = currentItem.displaySubtitle,
                            style = Typography.bodySmall.copy(color = MlhkTextSub, fontSize = 13.sp)
                        )
                    }

                    if (currentItem.genres.isNotEmpty()) {
                        Text(
                            text = currentItem.genres.take(2).joinToString(" • "),
                            style = Typography.bodySmall.copy(color = MlhkTextMuted, fontSize = 13.sp)
                        )
                    }
                }

                // Synopsis
                Text(
                    text = currentItem.summary ?: "Ervaar deze meeslepende titel in bioscoopkwaliteit via je Plex Media Server.",
                    style = Typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 21.sp),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.weight(1f))

                // Actie Knoppen
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeroActionButton(
                        text = if (currentItem.progress > 0) "▶ Verder kijken" else "▶ Nu kijken",
                        isPrimary = true,
                        onClick = { onPlayClick(currentItem) }
                    )

                    HeroActionButton(
                        text = if (inWatchlist) "✓ In mijn lijst" else "+ Mijn lijst",
                        isPrimary = false,
                        onClick = { inWatchlist = !inWatchlist }
                    )
                }
            }

            // Rechtsonder: 3 tot 5 interactieve Highlight Indicator Pills
            if (highlights.size > 1) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 48.dp, bottom = 24.dp)
                ) {
                    highlights.forEachIndexed { idx, _ ->
                        val isSelected = idx == validIndex
                        val pillInteraction = remember { MutableInteractionSource() }
                        val isPillFocused by pillInteraction.collectIsFocusedAsState()

                        val pillWidth = if (isSelected || isPillFocused) 28.dp else 10.dp
                        val pillColor = when {
                            isSelected -> MlhkNeonPink
                            isPillFocused -> MlhkNeonCyan
                            else -> MlhkTextMuted.copy(alpha = 0.45f)
                        }

                        Box(
                            modifier = Modifier
                                .width(pillWidth)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(pillColor)
                                .clickable(interactionSource = pillInteraction, indication = null) {
                                    currentIndex = idx
                                }
                                .focusable(interactionSource = pillInteraction)
                        )
                    }
                }
            }
        } else {
            // Elegant MLHK Skeleton Placeholder (pre-allocates 340dp so the interface never jumps!)
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxHeight()
                    .width(680.dp)
                    .padding(start = 48.dp, top = 28.dp, bottom = 18.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MlhkSurfaceHover.copy(alpha = 0.6f))
                )
                Box(
                    modifier = Modifier
                        .width(420.dp)
                        .height(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MlhkCardBg)
                        .border(1.dp, MlhkCardBorder, RoundedCornerShape(10.dp))
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .width(70.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MlhkSurfaceHover.copy(alpha = 0.6f))
                    )
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MlhkSurfaceHover.copy(alpha = 0.6f))
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MlhkSurface.copy(alpha = 0.4f))
                )
                Spacer(modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(MlhkPrimary.copy(alpha = 0.4f))
                    )
                    Box(
                        modifier = Modifier
                            .width(130.dp)
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(MlhkSurfaceHover.copy(alpha = 0.4f))
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
