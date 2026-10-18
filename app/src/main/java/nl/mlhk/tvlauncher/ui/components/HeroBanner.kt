package nl.mlhk.tvlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(290.dp)
    ) {
        // Backdrop Image
        if (item?.artUrl != null) {
            AsyncImage(
                model = item.artUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
            )
        }

        // Gradient Vignettes for text readability (matches MLHK dark theme)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MlhkBackground,
                            MlhkBackground.copy(alpha = 0.92f),
                            MlhkBackground.copy(alpha = 0.65f),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = 1400f
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
                            MlhkBackground.copy(alpha = 0.7f),
                            MlhkBackground
                        ),
                        startY = 0f,
                        endY = 700f
                    )
                )
        )

        // Banner Content
        if (item != null) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxHeight()
                    .width(620.dp)
                    .padding(start = 48.dp, top = 28.dp, bottom = 16.dp)
            ) {
                // Category or Series tag
                if (item.showTitle != null) {
                    Text(
                        text = item.showTitle.uppercase(),
                        style = Typography.labelMedium.copy(
                            color = MlhkNeonPink,
                            letterSpacing = 1.2.sp
                        )
                    )
                }

                // Title
                Text(
                    text = item.title,
                    style = Typography.displayLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Metadata chips
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (item.year != null) {
                        Text(
                            text = item.year.toString(),
                            style = Typography.bodySmall.copy(color = MlhkTextSub)
                        )
                    }

                    if (item.displaySubtitle.isNotBlank() && item.showTitle == null) {
                        Text(
                            text = item.displaySubtitle,
                            style = Typography.bodySmall.copy(color = MlhkTextSub)
                        )
                    }

                    if (item.rating != null && item.rating > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MlhkSurfaceHover)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "★ ${String.format("%.1f", item.rating)}",
                                style = Typography.bodySmall.copy(color = StatusOrange)
                            )
                        }
                    }

                    if (item.genres.isNotEmpty()) {
                        Text(
                            text = item.genres.take(2).joinToString(" • "),
                            style = Typography.bodySmall.copy(color = MlhkTextMuted)
                        )
                    }
                }

                // Synopsis
                if (!item.summary.isNullOrBlank()) {
                    Text(
                        text = item.summary,
                        style = Typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Action CTA
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeroButton(
                        text = if (item.progress > 0) "▶ Hervatten" else "▶ Afspelen",
                        isPrimary = true,
                        onClick = { onPlayClick(item) }
                    )
                }
            }
        } else {
            // Calm fallback banner when Plex has not loaded yet
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 48.dp, bottom = 16.dp)
            ) {
                Text(
                    text = "Welkom bij MLHK TV+",
                    style = Typography.displayLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Jouw films, series en zenders naadloos samengebracht.",
                    style = Typography.bodyMedium.copy(color = MlhkTextSub)
                )
            }
        }
    }
}

@Composable
private fun HeroButton(
    text: String,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bg = when {
        isFocused -> MlhkNeonPinkHover
        isPrimary -> MlhkPrimary
        else -> MlhkSurfaceHover
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, if (isFocused) MlhkTextMain else Color.Transparent, RoundedCornerShape(12.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 24.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            style = Typography.titleMedium.copy(
                color = MlhkBackground,
                fontSize = 15.sp
            )
        )
    }
}
