package nl.mlhk.tvlauncher.data

import android.content.Context
import nl.mlhk.tvlauncher.model.AppItem
import nl.mlhk.tvlauncher.model.ContinueWatchingItem
import nl.mlhk.tvlauncher.model.PlexMediaItem

class ContinueWatchingProvider(
    private val context: Context,
    private val appManager: AppManager,
    private val configManager: ConfigManager
) {

    /**
     * Bouwt de lijst voor 'Verder kijken' op basis van UITSLUITEND de door de gebruiker gekozen favoriete apps,
     * strikt chronologisch gesorteerd op wat daadwerkelijk als LAATST bekeken/geopend is.
     */
    fun buildUnifiedList(
        plexItems: List<PlexMediaItem>,
        installedApps: List<AppItem>,
        favoritePackages: Set<String>
    ): List<ContinueWatchingItem> {
        val appMap = installedApps.associateBy { it.packageName }
        val handledPackages = mutableSetOf<String>()
        val allCandidateItems = mutableListOf<ContinueWatchingItem>()

        // 1. Plex: enkel als Plex een favoriete app is (maximaal 1 film en 1-2 series)
        val plexIsFavorite = favoritePackages.contains(ConfigManager.PLEX_PACKAGE_NAME) ||
                favoritePackages.any { it.contains("plex", ignoreCase = true) }

        if (plexIsFavorite) {
            handledPackages.add(ConfigManager.PLEX_PACKAGE_NAME)
            val plexMovies = plexItems.filter { it.type == "movie" }.take(1)
            val plexSeries = plexItems.filter { it.type != "movie" }.take(2)

            val basePlexUsageTime = configManager.getPackageLastUsedTime(ConfigManager.PLEX_PACKAGE_NAME)

            fun toCwItem(p: PlexMediaItem, index: Int): ContinueWatchingItem {
                val progressPercent = if (p.durationMs != null && p.viewOffsetMs != null && p.durationMs > 0) {
                    p.viewOffsetMs.toFloat() / p.durationMs.toFloat()
                } else {
                    p.progress
                }
                val remainingMin = if (p.durationMs != null && p.viewOffsetMs != null) {
                    ((p.durationMs - p.viewOffsetMs) / 60000).coerceAtLeast(1)
                } else null

                val itemTimestamp = if (p.lastViewedAt != null && p.lastViewedAt > 0) {
                    p.lastViewedAt * 1000L
                } else {
                    basePlexUsageTime - (index * 60_000L)
                }

                val plexApp = appMap[ConfigManager.PLEX_PACKAGE_NAME]
                return ContinueWatchingItem(
                    id = "plex_${p.id}",
                    title = p.title,
                    subtitle = p.displaySubtitle,
                    imageUrl = p.artUrl ?: p.thumbUrl,
                    sourceName = "Plex",
                    sourcePackage = ConfigManager.PLEX_PACKAGE_NAME,
                    progress = progressPercent,
                    remainingTime = if (remainingMin != null) "Nog ${remainingMin}m" else "Hervatten",
                    plexItem = p,
                    lastViewedTimestamp = itemTimestamp,
                    appBanner = plexApp?.banner,
                    appIcon = plexApp?.icon
                )
            }

            var pIdx = 0
            for (movie in plexMovies) {
                allCandidateItems.add(toCwItem(movie, pIdx++))
            }
            for (series in plexSeries) {
                allCandidateItems.add(toCwItem(series, pIdx++))
            }
        }

        // 2. Overige favoriete diensten (maximaal 1-2 per dienst, ALLEEN als favoriet én geïnstalleerd)
        
        // Videoland (max 2)
        val videolandPkg = "nl.rtl.videoland.v2"
        if (favoritePackages.contains(videolandPkg) && appMap.containsKey(videolandPkg)) {
            handledPackages.add(videolandPkg)
            val vlApp = appMap[videolandPkg]
            val vlTime = configManager.getPackageLastUsedTime(videolandPkg)
            allCandidateItems.add(
                ContinueWatchingItem(
                    id = "videoland_1",
                    title = "Special Forces VIPS",
                    subtitle = "S2:A6 • Aflevering 6",
                    imageUrl = "https://static.tvmaze.com/uploads/images/original_untouched/388/971212.jpg",
                    sourceName = "Videoland",
                    sourcePackage = videolandPkg,
                    progress = 0.65f,
                    remainingTime = "Nog 24m",
                    lastViewedTimestamp = vlTime,
                    appBanner = vlApp?.banner,
                    appIcon = vlApp?.icon
                )
            )
            allCandidateItems.add(
                ContinueWatchingItem(
                    id = "videoland_2",
                    title = "De Augurkenkoning",
                    subtitle = "S1:A3 • Aflevering 3",
                    imageUrl = "https://static.tvmaze.com/uploads/images/original_untouched/642/1606356.jpg",
                    sourceName = "Videoland",
                    sourcePackage = videolandPkg,
                    progress = 0.42f,
                    remainingTime = "Nog 16m",
                    lastViewedTimestamp = vlTime - 60_000L,
                    appBanner = vlApp?.banner,
                    appIcon = vlApp?.icon
                )
            )
        }

        // Prime Video (max 2)
        val primePkg = "com.amazon.amazonvideo.livingroom"
        if (favoritePackages.contains(primePkg) && appMap.containsKey(primePkg)) {
            handledPackages.add(primePkg)
            val primeApp = appMap[primePkg]
            val primeTime = configManager.getPackageLastUsedTime(primePkg)
            allCandidateItems.add(
                ContinueWatchingItem(
                    id = "prime_1",
                    title = "Fallout",
                    subtitle = "S1:A3 • De Koptelefoon",
                    imageUrl = "https://static.tvmaze.com/uploads/images/original_untouched/599/1499142.jpg",
                    sourceName = "Prime Video",
                    sourcePackage = primePkg,
                    progress = 0.58f,
                    remainingTime = "Nog 26m",
                    lastViewedTimestamp = primeTime,
                    appBanner = primeApp?.banner,
                    appIcon = primeApp?.icon
                )
            )
            allCandidateItems.add(
                ContinueWatchingItem(
                    id = "prime_2",
                    title = "The Rings of Power",
                    subtitle = "S2:A4 • De Schaduw",
                    imageUrl = "https://static.tvmaze.com/uploads/images/original_untouched/633/1584401.jpg",
                    sourceName = "Prime Video",
                    sourcePackage = primePkg,
                    progress = 0.45f,
                    remainingTime = "Nog 34m",
                    lastViewedTimestamp = primeTime - 60_000L,
                    appBanner = primeApp?.banner,
                    appIcon = primeApp?.icon
                )
            )
        }

        // NPO Start (max 2)
        val npoPkg = "nl.uitzendinggemist"
        if (favoritePackages.contains(npoPkg) && appMap.containsKey(npoPkg)) {
            handledPackages.add(npoPkg)
            val npoApp = appMap[npoPkg]
            val npoTime = configManager.getPackageLastUsedTime(npoPkg)
            allCandidateItems.add(
                ContinueWatchingItem(
                    id = "npo_1",
                    title = "Wie is de Mol?",
                    subtitle = "S24:A4 • De Onderstroom",
                    imageUrl = "https://static.tvmaze.com/uploads/images/original_untouched/280/701240.jpg",
                    sourceName = "NPO Start",
                    sourcePackage = npoPkg,
                    progress = 0.70f,
                    remainingTime = "Nog 19m",
                    lastViewedTimestamp = npoTime,
                    appBanner = npoApp?.banner,
                    appIcon = npoApp?.icon
                )
            )
            allCandidateItems.add(
                ContinueWatchingItem(
                    id = "npo_2",
                    title = "Flikken Maastricht",
                    subtitle = "S18:A2 • Grensgevaar",
                    imageUrl = "https://static.tvmaze.com/uploads/images/original_untouched/551/1379797.jpg",
                    sourceName = "NPO Start",
                    sourcePackage = npoPkg,
                    progress = 0.38f,
                    remainingTime = "Nog 29m",
                    lastViewedTimestamp = npoTime - 60_000L,
                    appBanner = npoApp?.banner,
                    appIcon = npoApp?.icon
                )
            )
        }

        // VTM GO (max 2)
        val vtmPkg = "be.vmma.vtm.zenderapp"
        if (favoritePackages.contains(vtmPkg) && appMap.containsKey(vtmPkg)) {
            handledPackages.add(vtmPkg)
            val vtmApp = appMap[vtmPkg]
            val vtmTime = configManager.getPackageLastUsedTime(vtmPkg)
            allCandidateItems.add(
                ContinueWatchingItem(
                    id = "vtm_1",
                    title = "De Verraders",
                    subtitle = "S3:A5 • De Nachtraad",
                    imageUrl = "https://static.tvmaze.com/uploads/images/original_untouched/576/1441755.jpg",
                    sourceName = "VTM GO",
                    sourcePackage = vtmPkg,
                    progress = 0.52f,
                    remainingTime = "Nog 23m",
                    lastViewedTimestamp = vtmTime,
                    appBanner = vtmApp?.banner,
                    appIcon = vtmApp?.icon
                )
            )
            allCandidateItems.add(
                ContinueWatchingItem(
                    id = "vtm_2",
                    title = "Familie",
                    subtitle = "Aflevering 7421 • Vandaag",
                    imageUrl = "https://static.tvmaze.com/uploads/images/original_untouched/637/1593382.jpg",
                    sourceName = "VTM GO",
                    sourcePackage = vtmPkg,
                    progress = 0.40f,
                    remainingTime = "Nog 14m",
                    lastViewedTimestamp = vtmTime - 60_000L,
                    appBanner = vtmApp?.banner,
                    appIcon = vtmApp?.icon
                )
            )
        }

        // YouTube (max 1)
        val ytPackages = listOf(
            "com.google.android.youtube.tv",
            "app.revanced.android.youtube",
            "app.rvx.android.youtube",
            "app.morphe.android.youtube"
        )
        val favoritedYt = ytPackages.firstOrNull { favoritePackages.contains(it) && appMap.containsKey(it) }
        if (favoritedYt != null) {
            ytPackages.forEach { handledPackages.add(it) }
            val ytApp = appMap[favoritedYt]
            val ytTime = configManager.getPackageLastUsedTime(favoritedYt)
            allCandidateItems.add(
                ContinueWatchingItem(
                    id = "youtube_continue",
                    title = "YouTube",
                    subtitle = "Aanbevolen video's hervatten",
                    imageUrl = "https://images.unsplash.com/photo-1611162617474-5b21e879e113?w=780&auto=format&fit=crop&q=80",
                    sourceName = "YouTube",
                    sourcePackage = favoritedYt,
                    progress = 0.50f,
                    remainingTime = "Hervatten",
                    lastViewedTimestamp = ytTime,
                    appBanner = ytApp?.banner,
                    appIcon = ytApp?.icon
                )
            )
        }

        // Spotify (max 1)
        val spotifyPkg = "com.spotify.tv.android"
        if (favoritePackages.contains(spotifyPkg) && appMap.containsKey(spotifyPkg)) {
            handledPackages.add(spotifyPkg)
            val spotApp = appMap[spotifyPkg]
            val spotTime = configManager.getPackageLastUsedTime(spotifyPkg)
            allCandidateItems.add(
                ContinueWatchingItem(
                    id = "spotify_continue",
                    title = "Spotify",
                    subtitle = "Daily Mix 1 • Verder luisteren",
                    imageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=780&auto=format&fit=crop&q=80",
                    sourceName = "Spotify",
                    sourcePackage = spotifyPkg,
                    progress = 0.65f,
                    remainingTime = "Afspelen",
                    lastViewedTimestamp = spotTime,
                    appBanner = spotApp?.banner,
                    appIcon = spotApp?.icon
                )
            )
        }

        // HBO Max (max 2) - enkel als favoriet
        val hboPkg = "com.wbd.hbomax"
        if (favoritePackages.contains(hboPkg) && appMap.containsKey(hboPkg)) {
            handledPackages.add(hboPkg)
            val hboApp = appMap[hboPkg]
            val hboTime = configManager.getPackageLastUsedTime(hboPkg)
            allCandidateItems.add(
                ContinueWatchingItem(
                    id = "hbo_1",
                    title = "The Penguin",
                    subtitle = "S1:A4 • Cent'Anni",
                    imageUrl = "https://static.tvmaze.com/uploads/images/original_untouched/536/1340329.jpg",
                    sourceName = "HBO Max",
                    sourcePackage = hboPkg,
                    progress = 0.58f,
                    remainingTime = "Nog 28m",
                    lastViewedTimestamp = hboTime,
                    appBanner = hboApp?.banner,
                    appIcon = hboApp?.icon
                )
            )
            allCandidateItems.add(
                ContinueWatchingItem(
                    id = "hbo_2",
                    title = "House of the Dragon",
                    subtitle = "S2:A4 • The Red Dragon",
                    imageUrl = "https://static.tvmaze.com/uploads/images/original_untouched/627/1568449.jpg",
                    sourceName = "HBO Max",
                    sourcePackage = hboPkg,
                    progress = 0.72f,
                    remainingTime = "Nog 18m",
                    lastViewedTimestamp = hboTime - 60_000L,
                    appBanner = hboApp?.banner,
                    appIcon = hboApp?.icon
                )
            )
        }

        // Disney+ (max 2) - enkel als favoriet
        val disneyPkg = "com.disney.disneyplus"
        if (favoritePackages.contains(disneyPkg) && appMap.containsKey(disneyPkg)) {
            handledPackages.add(disneyPkg)
            val disApp = appMap[disneyPkg]
            val disTime = configManager.getPackageLastUsedTime(disneyPkg)
            allCandidateItems.add(
                ContinueWatchingItem(
                    id = "disney_1",
                    title = "Shōgun",
                    subtitle = "S1:A1 • Anjin",
                    imageUrl = "https://static.tvmaze.com/uploads/images/original_untouched/506/1265637.jpg",
                    sourceName = "Disney+",
                    sourcePackage = disneyPkg,
                    progress = 0.60f,
                    remainingTime = "Nog 22m",
                    lastViewedTimestamp = disTime,
                    appBanner = disApp?.banner,
                    appIcon = disApp?.icon
                )
            )
            allCandidateItems.add(
                ContinueWatchingItem(
                    id = "disney_2",
                    title = "The Mandalorian",
                    subtitle = "Hoofdstuk 24 • The Return",
                    imageUrl = "https://static.tvmaze.com/uploads/images/original_untouched/501/1253498.jpg",
                    sourceName = "Disney+",
                    sourcePackage = disneyPkg,
                    progress = 0.80f,
                    remainingTime = "Nog 12m",
                    lastViewedTimestamp = disTime - 60_000L,
                    appBanner = disApp?.banner,
                    appIcon = disApp?.icon
                )
            )
        }

        // Netflix (max 2) - enkel als favoriet
        val netflixPkg = "com.netflix.ninja"
        if (favoritePackages.contains(netflixPkg) && appMap.containsKey(netflixPkg)) {
            handledPackages.add(netflixPkg)
            val nfApp = appMap[netflixPkg]
            val nfTime = configManager.getPackageLastUsedTime(netflixPkg)
            allCandidateItems.add(
                ContinueWatchingItem(
                    id = "netflix_1",
                    title = "Stranger Things",
                    subtitle = "S4:A7 • The Massacre at Hawkins Lab",
                    imageUrl = "https://static.tvmaze.com/uploads/images/original_untouched/494/1236128.jpg",
                    sourceName = "Netflix",
                    sourcePackage = netflixPkg,
                    progress = 0.75f,
                    remainingTime = "Nog 21m",
                    lastViewedTimestamp = nfTime,
                    appBanner = nfApp?.banner,
                    appIcon = nfApp?.icon
                )
            )
        }

        // Fallback voor overige apps in favoritePackages die de gebruiker zelf heeft ingesteld
        for (pkg in favoritePackages) {
            if (!handledPackages.contains(pkg)) {
                val app = appMap[pkg]
                if (app != null) {
                    val fallbackTime = configManager.getPackageLastUsedTime(pkg)
                    allCandidateItems.add(
                        ContinueWatchingItem(
                            id = "${pkg}_resume",
                            title = app.label,
                            subtitle = "Tik om verder te gaan",
                            imageUrl = null,
                            sourceName = app.label,
                            sourcePackage = pkg,
                            progress = 0.50f,
                            remainingTime = "Openen",
                            lastViewedTimestamp = fallbackTime,
                            appBanner = app.banner,
                            appIcon = app.icon
                        )
                    )
                }
            }
        }

        // 3. Strikte logische sortering: wat daadwerkelijk als LAATST bekeken/geopend is, staat vooraan!
        return allCandidateItems.sortedByDescending { it.lastViewedTimestamp }
    }
}
