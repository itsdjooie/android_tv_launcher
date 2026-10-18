package nl.mlhk.tvlauncher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
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
import nl.mlhk.tvlauncher.model.IndicatorState
import nl.mlhk.tvlauncher.model.SystemHealthResponse
import nl.mlhk.tvlauncher.ui.theme.*

@Composable
fun StatusIndicator(
    health: SystemHealthResponse,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val dotColor by animateColorAsState(
        targetValue = when (health.indicatorState) {
            IndicatorState.GREEN -> StatusGreen
            IndicatorState.ORANGE -> StatusOrange
            IndicatorState.RED -> StatusRed
            IndicatorState.GREY -> StatusGrey
        },
        label = "dotColor"
    )

    val displayText = when (health.indicatorState) {
        IndicatorState.GREEN -> "Alles online"
        IndicatorState.ORANGE -> "Aandacht vereist"
        IndicatorState.RED -> "Dienst offline"
        IndicatorState.GREY -> "Monitoring"
    }

    val backgroundColor = if (isFocused) MlhkSurfaceHover else Color.Transparent
    val borderColor = if (isFocused) MlhkNeonPink else Color.Transparent

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = displayText,
            style = Typography.bodySmall.copy(
                fontSize = 12.sp,
                color = if (isFocused) MlhkTextMain else MlhkTextMuted
            )
        )
    }
}
