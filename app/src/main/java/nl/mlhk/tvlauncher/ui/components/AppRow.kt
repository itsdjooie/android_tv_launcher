package nl.mlhk.tvlauncher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import nl.mlhk.tvlauncher.model.AppItem
import nl.mlhk.tvlauncher.ui.theme.*

@Composable
fun AppRow(
    title: String = "Mijn favoriete apps",
    apps: List<AppItem>,
    onAppClick: (AppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (apps.isEmpty()) return

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = Typography.titleLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(start = 48.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(apps, key = { it.packageName }) { app ->
                AppCardItem(app = app, onClick = { onAppClick(app) })
            }
        }
    }
}

@Composable
private fun AppCardItem(
    app: AppItem,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1.0f,
        label = "appScale"
    )

    val border = when {
        isFocused -> MlhkNeonPink
        else -> MlhkCardBorder
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(160.dp)
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
    ) {
        // 16:9 Landscape Card (Exact KPN DIW7022 ratio)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(160.dp)
                .height(96.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (isFocused) MlhkSurfaceHover else MlhkCardBg)
                .border(if (isFocused) 2.dp else 1.dp, border, RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            if (app.icon != null) {
                val bitmap = remember(app.packageName) {
                    try {
                        app.icon.toBitmap(120, 120).asImageBitmap()
                    } catch (e: Exception) {
                        null
                    }
                }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = app.label,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Text(
                    text = app.label.take(2).uppercase(),
                    style = Typography.titleMedium.copy(color = MlhkPrimary, fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = app.label,
            style = Typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = if (isFocused) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isFocused) MlhkTextMain else MlhkTextSub
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
