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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import nl.mlhk.tvlauncher.R
import nl.mlhk.tvlauncher.model.SystemHealthResponse
import nl.mlhk.tvlauncher.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class TopTab(val label: String) {
    LIVE_TV("Nu op TV"),
    GUIDE("TV Gids"),
    START("Start"),
    MOVIES_SERIES("Films & Series"),
    APPS("Apps"),
    SAVED("Opgeslagen")
}

@Composable
fun TopNavigationBar(
    selectedTab: TopTab,
    onTabSelected: (TopTab) -> Unit,
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
            .height(56.dp)
            .padding(horizontal = 48.dp)
    ) {
        // Left: MLHK Logo + Nav Tabs
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.mlhk_logo),
                contentDescription = "MLHK Logo",
                modifier = Modifier
                    .height(28.dp)
                    .wrapContentWidth()
            )

            Spacer(modifier = Modifier.width(6.dp))

            TopTab.values().forEach { tab ->
                NavTabItem(
                    tab = tab,
                    isSelected = tab == selectedTab,
                    onClick = { onTabSelected(tab) }
                )
            }
        }

        // Right: Status indicator, Settings, Clock
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatusIndicator(
                health = health,
                onClick = onStatusClick
            )

            // Settings icon button
            val settingsInteraction = remember { MutableInteractionSource() }
            val isSettingsFocused by settingsInteraction.collectIsFocusedAsState()

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSettingsFocused) MlhkSurfaceHover else Color.Transparent)
                    .border(1.dp, if (isSettingsFocused) MlhkNeonPink else Color.Transparent, RoundedCornerShape(10.dp))
                    .clickable(interactionSource = settingsInteraction, indication = null, onClick = onSettingsClick)
                    .focusable(interactionSource = settingsInteraction)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Instellingen",
                    tint = if (isSettingsFocused) MlhkTextMain else MlhkTextMuted,
                    modifier = Modifier.size(19.dp)
                )
            }

            // Clean Clock
            Text(
                text = currentTime,
                style = Typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    color = MlhkTextSub
                )
            )
        }
    }
}

@Composable
private fun NavTabItem(
    tab: TopTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val textColor = when {
        isFocused -> MlhkTextMain
        isSelected -> MlhkTextMain
        else -> MlhkTextMuted
    }

    val underlineColor = when {
        isFocused -> MlhkNeonPink
        isSelected -> MlhkPrimary
        else -> Color.Transparent
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = tab.label,
            style = Typography.titleMedium.copy(
                fontSize = 15.sp,
                color = textColor
            )
        )
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .width(if (isSelected || isFocused) 18.dp else 0.dp)
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(underlineColor)
        )
    }
}
