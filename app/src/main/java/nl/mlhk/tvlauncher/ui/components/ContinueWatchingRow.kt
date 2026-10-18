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
import coil.compose.AsyncImage
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(start = 48.dp)
        ) {
            Text(
                text = title,
                style = Typography.titleLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Plex • Videoland • Netflix • Prime",
                style = Typography.bodySmall.copy(color = MlhkNeonPink)
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
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
        targetValue = if (isFocused) 1.07f else 1.0f,
        label = "cwScale"
    )

    val border = when {
        isFocused -> MlhkNeonPink
        else -> MlhkCardBorder
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
                .clip(RoundedCornerShape(14.dp))
                .background(MlhkCardBg)
                .border(if (isFocused) 2.dp else 1.dp, border, RoundedCornerShape(14.dp))
        ) {
            if (item.imageUrl != null) {
                AsyncImage(
                    model = item.imageUrl,
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
                        text = item.sourceName.uppercase(),
                        style = Typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = when (item.sourceName.lowercase()) {
                                "netflix" -> StatusRed
                                "videoland" -> StatusRed
                                "prime video" -> MlhkNeonCyan
                                else -> MlhkPrimary
                            }
                        )
                    )
                }
            }

            // Top Badges: Source Badge + Remaining Time
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // App Badge Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.sourceName.uppercase(),
                        style = Typography.bodySmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (item.sourceName.lowercase()) {
                                "netflix" -> StatusRed
                                "videoland" -> StatusRed
                                "prime video" -> MlhkNeonCyan
                                else -> MlhkPrimary
                            }
                        )
                    )
                }

                if (item.remainingTime != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.8f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
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

            // Progress bar
            if (item.progress > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
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
