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
import nl.mlhk.tvlauncher.model.LiveTvChannel
import nl.mlhk.tvlauncher.ui.theme.*

@Composable
fun TvGuideDialog(
    channels: List<LiveTvChannel>,
    onSelectChannel: (LiveTvChannel) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(760.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MlhkCardBg)
                .border(1.dp, MlhkCardBorderGlow, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "TV Gids — Live & Programmering",
                            style = Typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MlhkTextMain)
                        )
                        Text(
                            text = "Klik op een zender om direct live te kijken",
                            style = Typography.bodySmall.copy(color = MlhkTextSub)
                        )
                    }

                    TvButton(
                        text = "Sluiten",
                        isPrimary = false,
                        onClick = onDismiss
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MlhkCardBorder)
                )

                if (channels.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Text(
                            text = "Geen IPTV-zenders geladen. Koppel je IPTV-provider in Instellingen.",
                            style = Typography.bodyMedium.copy(color = MlhkTextSub)
                        )
                    }
                } else {
                    // Channels List
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp)
                    ) {
                        items(channels, key = { it.channelNumber }) { channel ->
                            ChannelGuideItem(channel = channel, onClick = { onSelectChannel(channel) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelGuideItem(
    channel: LiveTvChannel,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bg = if (isFocused) MlhkSurfaceHover else MlhkSurface
    val border = if (isFocused) MlhkNeonPink else Color.Transparent

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isFocused) MlhkNeonPink.copy(alpha = 0.3f) else MlhkCardBg)
            ) {
                Text(
                    text = channel.channelNumber.toString(),
                    style = Typography.titleMedium.copy(
                        color = if (isFocused) MlhkNeonPink else MlhkTextMain,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Column {
                Text(
                    text = channel.name,
                    style = Typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MlhkTextMain)
                )
                Text(
                    text = channel.currentProgram,
                    style = Typography.bodySmall.copy(color = if (isFocused) MlhkNeonCyan else MlhkTextSub)
                )
            }
        }

        if (channel.timeSlot.isNotBlank()) {
            Text(
                text = channel.timeSlot,
                style = Typography.labelSmall.copy(color = MlhkTextMuted)
            )
        }
    }
}
