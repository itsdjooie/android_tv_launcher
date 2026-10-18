package nl.mlhk.tvlauncher.ui.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import nl.mlhk.tvlauncher.model.PlexMediaItem
import nl.mlhk.tvlauncher.ui.theme.*

@Composable
fun MediaCard(
    item: PlexMediaItem,
    isLandscape: Boolean = false,
    cardWidth: Dp = if (isLandscape) 240.dp else 135.dp,
    cardHeight: Dp = if (isLandscape) 135.dp else 200.dp,
    onFocusChange: (Boolean) -> Unit = {},
    onClick: (PlexMediaItem) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused) {
        onFocusChange(isFocused)
    }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.07f else 1.0f,
        label = "cardScale"
    )

    val borderColor = when {
        isFocused -> MlhkNeonPink
        else -> MlhkCardBorder
    }

    Column(
        modifier = Modifier
            .width(cardWidth)
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null) { onClick(item) }
            .focusable(interactionSource = interactionSource)
    ) {
        // Thumbnail / Poster Box
        Box(
            modifier = Modifier
                .width(cardWidth)
                .height(cardHeight)
                .clip(RoundedCornerShape(14.dp))
                .background(MlhkCardBg)
                .border(if (isFocused) 2.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
        ) {
            val imageUrl = if (isLandscape) (item.artUrl ?: item.thumbUrl) else (item.thumbUrl ?: item.artUrl)
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MlhkSurfaceHover)
                ) {
                    Text(
                        text = item.title.take(1),
                        style = Typography.displayLarge.copy(color = MlhkNeonPink, fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Top Badges
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                if (item.rating != null && item.rating > 0 && !isLandscape) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "★ ${String.format("%.1f", item.rating)}",
                            style = Typography.bodySmall.copy(
                                color = StatusOrange,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // 4K Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "4K",
                        style = Typography.bodySmall.copy(
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Watch Progress Bar (Verder kijken)
            if (item.progress > 0) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(item.progress)
                                .background(if (isFocused) MlhkNeonPink else MlhkPrimary)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        // Title
        Text(
            text = item.title,
            style = Typography.titleMedium.copy(
                fontSize = 13.sp,
                fontWeight = if (isFocused) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isFocused) MlhkTextMain else MlhkTextSub
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Subtitle (Show name / Episode or Year)
        if (item.displaySubtitle.isNotBlank()) {
            Text(
                text = item.displaySubtitle,
                style = Typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = MlhkTextMuted
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
