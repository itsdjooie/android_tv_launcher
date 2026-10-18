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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import nl.mlhk.tvlauncher.model.ServiceStatus
import nl.mlhk.tvlauncher.model.SystemHealthResponse
import nl.mlhk.tvlauncher.ui.theme.*

@Composable
fun StatusDialog(
    health: SystemHealthResponse,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(520.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MlhkCardBg)
                .border(1.dp, MlhkCardBorder, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Status van eigen diensten",
                        style = Typography.titleLarge
                    )
                    Text(
                        text = health.message,
                        style = Typography.bodySmall.copy(color = MlhkNeonPink)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MlhkCardBorder)
                )

                // Services List
                if (health.services.isEmpty()) {
                    Text(
                        text = "Geen statusgegevens beschikbaar. Controleer of de centrale monitoring-API draait.",
                        style = Typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                    ) {
                        items(health.services) { service ->
                            ServiceRowItem(service)
                        }
                    }
                }

                // Footer close button
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val interactionSource = remember { MutableInteractionSource() }
                    val isFocused by interactionSource.collectIsFocusedAsState()

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isFocused) MlhkPrimary else MlhkSurface)
                            .border(1.dp, if (isFocused) MlhkNeonPink else MlhkCardBorder, RoundedCornerShape(12.dp))
                            .clickable(interactionSource = interactionSource, indication = null, onClick = onDismiss)
                            .focusable(interactionSource = interactionSource)
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Sluiten",
                            style = Typography.labelMedium.copy(
                                color = if (isFocused) MlhkBackground else MlhkTextMain
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceRowItem(service: ServiceStatus) {
    val dotColor = when {
        service.isUp -> StatusGreen
        service.isDegraded -> StatusOrange
        service.isDown -> StatusRed
        else -> StatusGrey
    }

    val statusLabel = when {
        service.isUp -> "Online"
        service.isDegraded -> "Vertraagd"
        service.isDown -> "Offline"
        else -> "Onbekend"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MlhkSurface)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Text(
                text = service.name,
                style = Typography.titleMedium.copy(fontSize = 14.sp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (service.latencyMs > 0) {
                Text(
                    text = "${service.latencyMs} ms",
                    style = Typography.bodySmall.copy(color = MlhkTextMuted)
                )
            }
            Text(
                text = statusLabel,
                style = Typography.bodySmall.copy(
                    color = dotColor,
                    fontSize = 13.sp
                )
            )
        }
    }
}
