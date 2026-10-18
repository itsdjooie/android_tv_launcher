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
    onOpenNativeGuide: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(720.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MlhkCardBg)
                .border(1.dp, MlhkCardBorder, RoundedCornerShape(20.dp))
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
                            style = Typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Overzicht van Nederlandse zenders op je KPN TV+ Box",
                            style = Typography.bodySmall.copy(color = MlhkTextSub)
                        )
                    }

                    TvButton(
                        text = "Volledige KPN Gids",
                        isPrimary = false,
                        onClick = onOpenNativeGuide
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MlhkCardBorder)
                )

                // Channels List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                ) {
                    items(channels, key = { it.channelNumber }) { channel ->
                        ChannelGuideItem(channel = channel, onClick = { onSelectChannel(channel) })
                    }
                }

                // Footer
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TvButton(
                        text = "Sluiten",
                        isPrimary = true,
                        onClick = onDismiss
                    )
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
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isFocused) MlhkPrimary else MlhkCardBg)
            ) {
                Text(
                    text = channel.channelNumber.toString(),
                    style = Typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isFocused) MlhkBackground else MlhkTextMain
                    )
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = channel.name,
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "• ${channel.category}",
                        style = Typography.bodySmall.copy(color = MlhkNeonCyan)
                    )
                }
                Text(
                    text = channel.currentProgram,
                    style = Typography.bodyMedium.copy(
                        color = if (isFocused) MlhkTextMain else MlhkTextSub,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = channel.timeSlot,
                style = Typography.bodySmall.copy(color = MlhkTextMuted)
            )
            Text(
                text = "Hierna: ${channel.nextProgram}",
                style = Typography.bodySmall.copy(color = MlhkTextSub, fontSize = 11.sp)
            )
        }
    }
}
