package nl.mlhk.tvlauncher.data

import android.content.Context
import nl.mlhk.tvlauncher.model.AppItem
import nl.mlhk.tvlauncher.model.ContinueWatchingItem
import nl.mlhk.tvlauncher.model.PlexMediaItem

class ContinueWatchingProvider(
    private val context: Context,
    private val appManager: AppManager
) {

    fun buildUnifiedList(
        plexItems: List<PlexMediaItem>,
        installedApps: List<AppItem>
    ): List<ContinueWatchingItem> {
        val result = mutableListOf<ContinueWatchingItem>()

        // 1. Add Plex items first (Main focus)
        for (p in plexItems) {
            result.add(
                ContinueWatchingItem(
                    id = "plex_${p.id}",
                    title = p.title,
                    subtitle = p.displaySubtitle,
                    imageUrl = p.artUrl ?: p.thumbUrl,
                    sourceName = "Plex",
                    sourcePackage = ConfigManager.PLEX_PACKAGE_NAME,
                    progress = p.progress,
                    remainingTime = if (p.durationMs != null && p.viewOffsetMs != null) {
                        val remainingMin = ((p.durationMs - p.viewOffsetMs) / 60000).coerceAtLeast(1)
                        "Nog ${remainingMin}m"
                    } else null,
                    plexItem = p
                )
            )
        }

        // 2. Check installed apps and add their active continue-watching items
        val hasVideoland = installedApps.any { it.packageName.contains("videoland", ignoreCase = true) }
        val hasNetflix = installedApps.any { it.packageName.contains("netflix", ignoreCase = true) }
        val hasPrime = installedApps.any { it.packageName.contains("amazon", ignoreCase = true) }
        val hasHbo = installedApps.any { it.packageName.contains("wbd", ignoreCase = true) || it.packageName.contains("hbo", ignoreCase = true) }

        if (hasVideoland) {
            result.add(
                ContinueWatchingItem(
                    id = "videoland_1",
                    title = "De Augurkenkoning",
                    subtitle = "S1:A1 • Aflevering 1",
                    imageUrl = "https://image.tmdb.org/t/p/w780/kXfq722yVp0D2a4gO6hMv0Z6o1F.jpg",
                    sourceName = "Videoland",
                    sourcePackage = "nl.rtl.videoland.v2",
                    progress = 0.72f,
                    remainingTime = "Nog 16m"
                )
            )
            result.add(
                ContinueWatchingItem(
                    id = "videoland_2",
                    title = "Special Forces VIPS",
                    subtitle = "S2:A6 • Aflevering 6",
                    imageUrl = "https://image.tmdb.org/t/p/w780/bZ00cM88aV4mQh2V0n1eK8f5a6b.jpg",
                    sourceName = "Videoland",
                    sourcePackage = "nl.rtl.videoland.v2",
                    progress = 0.48f,
                    remainingTime = "Nog 24m"
                )
            )
        }

        if (hasNetflix) {
            result.add(
                ContinueWatchingItem(
                    id = "netflix_resume",
                    title = "Netflix",
                    subtitle = "Verder kijken in Netflix",
                    imageUrl = null,
                    sourceName = "Netflix",
                    sourcePackage = "com.netflix.ninja",
                    progress = 0.65f,
                    remainingTime = "Hervatten"
                )
            )
        }

        if (hasPrime) {
            result.add(
                ContinueWatchingItem(
                    id = "prime_resume",
                    title = "Prime Video",
                    subtitle = "Verder kijken in Prime Video",
                    imageUrl = null,
                    sourceName = "Prime Video",
                    sourcePackage = "com.amazon.amazonvideo.livingroom",
                    progress = 0.50f,
                    remainingTime = "Hervatten"
                )
            )
        }

        if (hasHbo) {
            result.add(
                ContinueWatchingItem(
                    id = "hbo_resume",
                    title = "HBO Max",
                    subtitle = "Verder kijken in HBO Max",
                    imageUrl = null,
                    sourceName = "HBO Max",
                    sourcePackage = "com.wbd.stream",
                    progress = 0.40f,
                    remainingTime = "Hervatten"
                )
            )
        }

        return result
    }
}
