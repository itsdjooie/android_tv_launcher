package nl.mlhk.tvlauncher.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.mlhk.tvlauncher.model.PlexMediaItem
import nl.mlhk.tvlauncher.ui.theme.Typography

@Composable
fun MediaRow(
    title: String,
    items: List<PlexMediaItem>,
    isLandscape: Boolean = false,
    onItemFocused: (PlexMediaItem) -> Unit = {},
    onItemClick: (PlexMediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    val listState = rememberLazyListState()

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = title,
            style = Typography.titleLarge.copy(fontSize = 17.sp),
            modifier = Modifier.padding(start = 48.dp)
        )

        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items, key = { it.id }) { item ->
                MediaCard(
                    item = item,
                    isLandscape = isLandscape,
                    onFocusChange = { focused ->
                        if (focused) {
                            onItemFocused(item)
                        }
                    },
                    onClick = onItemClick
                )
            }
        }
    }
}
