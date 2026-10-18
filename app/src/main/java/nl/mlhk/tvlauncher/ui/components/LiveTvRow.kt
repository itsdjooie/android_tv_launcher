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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.mlhk.tvlauncher.model.LiveTvChannel
import nl.mlhk.tvlauncher.ui.theme.*

@Composable
fun LiveTvRow(
    title: String = "Nu op TV (Live Zenders)",
    channels: List<LiveTvChannel>,
    onChannelClick: (LiveTvChannel) -> Unit,
    modifier: Modifier = Modifier
) {
    if (channels.isEmpty()) return

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
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(StatusRed)
            )
            Text(
                text = title,
                style = Typography.titleLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold)
            )
            Text(
                text = "KPN TV Live",
                style = Typography.bodySmall.copy(color = MlhkNeonPink)
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(channels, key = { it.channelNumber }) { channel ->
                LiveChannelCard(channel = channel, onClick = { onChannelClick(channel) })
            }
        }
    }
}

@Composable
private fun LiveChannelCard(
    channel: LiveTvChannel,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.07f else 1.0f,
        label = "liveScale"
    )

    val border = when {
        isFocused -> MlhkNeonPink
        else -> MlhkCardBorder
    }

    val bgGradient = if (isFocused) {
        Brush.verticalGradient(listOf(MlhkSurfaceHover, MlhkCardBg))
    } else {
        Brush.verticalGradient(listOf(MlhkCardBg, MlhkSurface))
    }

    Column(
        modifier = Modifier
            .width(230.dp)
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
    ) {
        Box(
            modifier = Modifier
                .width(230.dp)
                .height(130.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(bgGradient)
                .border(if (isFocused) 2.dp else 1.dp, border, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Row: Channel Number Pill + LIVE Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isFocused) MlhkPrimary else MlhkSurfaceHover)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = channel.channelNumber.toString(),
                                style = Typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isFocused) MlhkBackground else MlhkTextMain
                                )
                            )
                        }
                        Text(
                            text = channel.name,
                            style = Typography.titleMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    // LIVE Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(StatusRed.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(StatusRed)
                        )
                        Text(
                            text = "LIVE",
                            style = Typography.bodySmall.copy(
                                color = StatusRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // Middle: Program Title
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = channel.currentProgram,
                        style = Typography.titleMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isFocused) MlhkTextMain else MlhkTextSub
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Hierna: ${channel.nextProgram}",
                        style = Typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = MlhkTextMuted
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Bottom: Progress Bar & Timeslot
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = channel.timeSlot,
                            style = Typography.bodySmall.copy(fontSize = 10.sp, color = MlhkTextMuted)
                        )
                        Text(
                            text = "${(channel.progress * 100).toInt()}%",
                            style = Typography.bodySmall.copy(fontSize = 10.sp, color = MlhkNeonPink)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(channel.progress)
                                .background(if (isFocused) MlhkNeonPink else MlhkPrimary)
                        )
                    }
                }
            }
        }
    }
}
