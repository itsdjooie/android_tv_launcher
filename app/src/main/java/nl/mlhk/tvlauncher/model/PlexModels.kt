package nl.mlhk.tvlauncher.model

data class PlexMediaItem(
    val id: String,
    val ratingKey: String,
    val key: String,
    val title: String,
    val type: String, // "movie", "episode", "season", "show"
    val thumbUrl: String?,
    val artUrl: String?,
    val year: Int? = null,
    val summary: String? = null,
    val durationMs: Long? = null,
    val viewOffsetMs: Long? = null,
    val showTitle: String? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val rating: Float? = null,
    val genres: List<String> = emptyList()
) {
    val progress: Float
        get() {
            if (durationMs != null && durationMs > 0 && viewOffsetMs != null && viewOffsetMs > 0) {
                return (viewOffsetMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
            }
            return 0f
        }

    val displaySubtitle: String
        get() {
            return when {
                showTitle != null && seasonNumber != null && episodeNumber != null ->
                    "$showTitle • S${seasonNumber}E${episodeNumber}"
                showTitle != null -> showTitle
                year != null -> year.toString()
                else -> ""
            }
        }
}

data class PlexSectionHub(
    val title: String,
    val items: List<PlexMediaItem>
)
