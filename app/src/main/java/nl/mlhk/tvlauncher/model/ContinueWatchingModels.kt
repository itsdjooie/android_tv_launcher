package nl.mlhk.tvlauncher.model

data class ContinueWatchingItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String?,
    val sourceName: String, // "Plex", "Videoland", "Netflix", "Prime Video", "HBO Max", "YouTube"
    val sourcePackage: String,
    val progress: Float, // 0.0 to 1.0
    val remainingTime: String? = null,
    val plexItem: PlexMediaItem? = null,
    val lastViewedTimestamp: Long = 0L
)
