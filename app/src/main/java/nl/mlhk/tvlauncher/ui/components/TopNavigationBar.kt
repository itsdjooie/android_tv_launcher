package nl.mlhk.tvlauncher.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import nl.mlhk.tvlauncher.R
import nl.mlhk.tvlauncher.model.SystemHealthResponse
import nl.mlhk.tvlauncher.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class TopTab(val label: String) {
    START("Start"),
    LIVE_TV("Nu op TV"),
    GUIDE("TV Gids"),
    MOVIES_SERIES("Films & Series"),
    SPORT("Sport"),
    APPS("Apps"),
    SAVED("Opgeslagen")
}

@Composable
fun TopNavigationBar(
    selectedTab: TopTab,
    onTabSelected: (TopTab) -> Unit,
    onSearchClick: () -> Unit,
    health: SystemHealthResponse,
    onStatusClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTime by remember {
        mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            delay(30_000)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 48.dp)
    ) {
        // Left: MLHK Logo + Zoeken + Menu Tabs (Matches KPN DIW7022 exact layout)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.mlhk_logo),
                contentDescription = "MLHK Logo",
                modifier = Modifier
                    .height(30.dp)
                    .wrapContentWidth()
            )

            // Search Icon Button
            NavIconButton(
                icon = Icons.Default.Search,
                label = "Zoeken",
                onClick = onSearchClick
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Navigation Tabs with KPN Pill shape
            TopTab.values().forEach { tab ->
                NavTabPill(
                    tab = tab,
                    isSelected = tab == selectedTab,
                    onClick = { onTabSelected(tab) }
                )
            }
        }

        // Right: Status indicator, Settings, Digital Clock
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            StatusIndicator(
                health = health,
                onClick = onStatusClick
            )

            NavIconButton(
                icon = Icons.Default.Settings,
                label = "Instellingen",
                onClick = onSettingsClick
            )

            // Digital Clock
            Text(
                text = currentTime,
                style = Typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MlhkTextMain
                )
            )
        }
    }
}

@Composable
private fun NavTabPill(
    tab: TopTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bg = when {
        isFocused -> MlhkPrimary
        isSelected -> MlhkSurfaceHover
        else -> Color.Transparent
    }

    val textColor = when {
        isFocused -> MlhkBackground
        isSelected -> MlhkTextMain
        else -> MlhkTextMuted
    }

    val border = when {
        isFocused -> MlhkNeonPink
        isSelected -> MlhkCardBorder
        else -> Color.Transparent
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = tab.label,
            style = Typography.labelMedium.copy(
                fontSize = 14.sp,
                fontWeight = if (isSelected || isFocused) FontWeight.SemiBold else FontWeight.Normal,
                color = textColor
            )
        )
    }
}

@Composable
private fun NavIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isFocused) MlhkPrimary else Color.Transparent)
            .border(1.dp, if (isFocused) MlhkNeonPink else Color.Transparent, RoundedCornerShape(18.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isFocused) MlhkBackground else MlhkTextMuted,
            modifier = Modifier.size(19.dp)
        )
    }
}
