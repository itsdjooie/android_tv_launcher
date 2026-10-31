package nl.mlhk.tvlauncher.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
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
    LIVE_TV("Live TV"),
    GUIDE("TV Gids"),
    MOVIES_SERIES("Films & Series"),
    APPS("Apps")
}

@Composable
fun TopNavigationBar(
    selectedTab: TopTab,
    isIptvConfigured: Boolean,
    onTabSelected: (TopTab) -> Unit,
    onSearchClick: () -> Unit,
    health: SystemHealthResponse,
    onStatusClick: () -> Unit,
    onSettingsClick: () -> Unit,
    roomName: String = "",
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

    val visibleTabs = remember(isIptvConfigured) {
        if (isIptvConfigured) {
            listOf(TopTab.START, TopTab.LIVE_TV, TopTab.GUIDE, TopTab.MOVIES_SERIES, TopTab.APPS)
        } else {
            listOf(TopTab.START, TopTab.MOVIES_SERIES, TopTab.APPS)
        }
    }



    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 48.dp)
        ) {
            // Left: MLHK Logo + LIVE Badge + Zoeken + Menu Tabs
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.mlhk_logo),
                        contentDescription = "MLHK Logo",
                        modifier = Modifier
                            .height(30.dp)
                            .wrapContentWidth()
                    )

                    // MLHK.nl signature LIVE 2026 Badge
                    val displayBadgeText = if (roomName.isNotBlank() && !roomName.equals("KPN TV+", ignoreCase = true)) {
                        "2026 TV+ • ${roomName.uppercase()}"
                    } else {
                        "2026 TV+"
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MlhkCardBg)
                            .border(1.dp, MlhkNeonPink.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(MlhkNeonPink)
                            )
                            Text(
                                text = displayBadgeText,
                                style = Typography.labelSmall.copy(
                                    color = MlhkNeonCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }
                }

                // Search Icon Button
                NavIconButton(
                    icon = Icons.Default.Search,
                    label = "Zoeken",
                    onClick = onSearchClick
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Navigation Tabs with MLHK Glassmorphism Pill shape
                visibleTabs.forEach { tab ->
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
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MlhkCardBg)
                        .border(1.dp, MlhkCardBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = currentTime,
                        style = Typography.titleMedium.copy(
                            color = MlhkTextMain,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        }

        // Signature MLHK glowing accent line below the navbar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(MlhkAccentLineGradient)
        )
    }
}

@Composable
private fun NavIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isFocused) MlhkNeonPink.copy(alpha = 0.25f) else MlhkCardBg)
            .border(
                1.dp,
                if (isFocused) MlhkNeonPink else MlhkCardBorder,
                RoundedCornerShape(20.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isFocused) MlhkNeonPink else MlhkTextSub,
            modifier = Modifier.size(19.dp)
        )
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

    val background = when {
        isFocused -> MlhkNeonPink.copy(alpha = 0.3f)
        isSelected -> MlhkSurfaceHover
        else -> Color.Transparent
    }

    val border = when {
        isFocused -> MlhkNeonPink
        isSelected -> MlhkCardBorderGlow
        else -> Color.Transparent
    }

    val textColor = when {
        isFocused -> MlhkTextMain
        isSelected -> MlhkNeonCyan
        else -> MlhkTextSub
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 18.dp)
    ) {
        Text(
            text = tab.label,
            style = Typography.bodyMedium.copy(
                color = textColor,
                fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
            )
        )
    }
}
