package nl.mlhk.tvlauncher.model

import android.graphics.drawable.Drawable

data class ContinueWatchingItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String?,
    val sourceName: String, // "Plex", "Videoland", "Netflix", "Prime Video", "HBO Max", "YouTube", etc.
    val sourcePackage: String,
    val progress: Float, // 0.0 to 1.0
    val remainingTime: String? = null,
    val plexItem: PlexMediaItem? = null,
    val lastViewedTimestamp: Long = 0L,
    val appBanner: Drawable? = null,
    val appIcon: Drawable? = null
)
