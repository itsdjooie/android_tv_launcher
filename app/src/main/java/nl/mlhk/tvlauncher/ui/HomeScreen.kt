package nl.mlhk.tvlauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.mlhk.tvlauncher.data.AppManager
import nl.mlhk.tvlauncher.data.ConfigManager
import nl.mlhk.tvlauncher.data.LiveTvProvider
import nl.mlhk.tvlauncher.data.PlexClient
import nl.mlhk.tvlauncher.data.StatusClient
import nl.mlhk.tvlauncher.model.AppItem
import nl.mlhk.tvlauncher.model.LiveTvChannel
import nl.mlhk.tvlauncher.model.PlexMediaItem
import nl.mlhk.tvlauncher.model.SystemHealthResponse
import nl.mlhk.tvlauncher.ui.components.*
import nl.mlhk.tvlauncher.ui.theme.MlhkBackground
import nl.mlhk.tvlauncher.ui.theme.MlhkNeonCyan
import nl.mlhk.tvlauncher.ui.theme.MlhkNeonPink
import nl.mlhk.tvlauncher.ui.theme.MlhkNeonPurple

@Composable
fun HomeScreen(
    configManager: ConfigManager,
    plexClient: PlexClient,
    statusClient: StatusClient,
    appManager: AppManager,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var selectedTab by remember { mutableStateOf(TopTab.START) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showTvGuideDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }

    var health by remember { mutableStateOf(SystemHealthResponse()) }
    var liveChannels by remember { mutableStateOf<List<LiveTvChannel>>(emptyList()) }
    var continueWatchingItems by remember { mutableStateOf<List<PlexMediaItem>>(emptyList()) }
    var recentlyAddedItems by remember { mutableStateOf<List<PlexMediaItem>>(emptyList()) }
    var recommendedItems by remember { mutableStateOf<List<PlexMediaItem>>(emptyList()) }
    var movieItems by remember { mutableStateOf<List<PlexMediaItem>>(emptyList()) }
    var seriesItems by remember { mutableStateOf<List<PlexMediaItem>>(emptyList()) }
    var apps by remember { mutableStateOf<List<AppItem>>(emptyList()) }

    var heroItem by remember { mutableStateOf<PlexMediaItem?>(null) }

    fun loadContent() {
        coroutineScope.launch {
            // Live TV Channels
            liveChannels = LiveTvProvider.getChannels()

            // Fetch Status
            try {
                health = statusClient.getSystemStatus()
            } catch (e: Exception) {
                health = SystemHealthResponse(status = "grey", message = "Monitoring offline")
            }

            // Fetch Apps
            try {
                apps = appManager.getInstalledApps()
            } catch (e: Exception) {
                apps = emptyList()
            }

            // Fetch Plex Rows (with rich fallback previews)
            try {
                val cw = plexClient.getContinueWatching()
                val ra = plexClient.getRecentlyAdded()
                val rec = plexClient.getRecommended()
                val mov = plexClient.getMovies()
                val ser = plexClient.getSeries()

                continueWatchingItems = cw
                recentlyAddedItems = ra
                recommendedItems = rec
                movieItems = mov
                seriesItems = ser

                if (heroItem == null) {
                    heroItem = ra.firstOrNull() ?: cw.firstOrNull() ?: rec.firstOrNull()
                }
            } catch (e: Exception) {
                // error handled
            }
        }
    }

    LaunchedEffect(Unit) {
        loadContent()
    }

    // Background 60s status refresh loop
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            try {
                health = statusClient.getSystemStatus()
                liveChannels = LiveTvProvider.getChannels() // update progress
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    val allPlexItems = remember(continueWatchingItems, recentlyAddedItems, recommendedItems, movieItems, seriesItems) {
        (continueWatchingItems + recentlyAddedItems + recommendedItems + movieItems + seriesItems).distinctBy { it.id }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MlhkBackground)
    ) {
        // MLHK Next-Gen Ambient Aurora Background Effect (from mlhk-source.css)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(MlhkNeonPink.copy(alpha = 0.08f), Color.Transparent),
                        radius = 800f
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(MlhkNeonPurple.copy(alpha = 0.07f), Color.Transparent),
                        radius = 1100f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // 1. Top Navigation Bar (Matches KPN TV+ layout with MLHK styling)
            TopNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                    when (tab) {
                        TopTab.START -> {
                            coroutineScope.launch { scrollState.animateScrollTo(0) }
                        }
                        TopTab.LIVE_TV -> {
                            appManager.launchKpnTv("live")
                        }
                        TopTab.GUIDE -> {
                            showTvGuideDialog = true
                        }
                        TopTab.MOVIES_SERIES -> {
                            coroutineScope.launch { scrollState.animateScrollTo(1000) }
                        }
                        TopTab.SPORT -> {
                            appManager.launchKpnTv("sport")
                        }
                        TopTab.APPS -> {
                            coroutineScope.launch { scrollState.animateScrollTo(420) }
                        }
                        TopTab.SAVED -> {
                            appManager.launchKpnTv("recordings")
                        }
                    }
                },
                onSearchClick = { showSearchDialog = true },
                health = health,
                onStatusClick = { showStatusDialog = true },
                onSettingsClick = { showSettingsDialog = true }
            )

            // 2. Cinematic Hero Banner (Plex featured item)
            HeroBanner(
                item = heroItem,
                onPlayClick = { item -> appManager.launchPlexMedia(item) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Nu op TV (Live Zenders Row - KPN TV Feature)
            LiveTvRow(
                title = "Nu op TV (Live Zenders)",
                channels = liveChannels,
                onChannelClick = { channel ->
                    appManager.launchKpnTv("channel_${channel.channelNumber}")
                }
            )

            // 4. Mijn favoriete apps Row (Exact KPN DIW7022 16:9 cards)
            AppRow(
                title = "Mijn favoriete apps",
                apps = apps,
                onAppClick = { app -> appManager.launchApp(app.packageName) }
            )

            // 5. Verder kijken (Plex Continue Watching with Progress Bar)
            if (continueWatchingItems.isNotEmpty()) {
                MediaRow(
                    title = "Verder kijken in Plex",
                    items = continueWatchingItems,
                    isLandscape = true,
                    onItemFocused = { heroItem = it },
                    onItemClick = { item -> appManager.launchPlexMedia(item) }
                )
            }

            // 6. Recent toegevoegd (Plex Recently Added)
            if (recentlyAddedItems.isNotEmpty()) {
                MediaRow(
                    title = "Recent toegevoegd in Plex",
                    items = recentlyAddedItems,
                    isLandscape = false,
                    onItemFocused = { heroItem = it },
                    onItemClick = { item -> appManager.launchPlexMedia(item) }
                )
            }

            // 7. Aanbevolen voor jou (Plex Recommendations)
            if (recommendedItems.isNotEmpty()) {
                MediaRow(
                    title = "Aanbevolen films & series",
                    items = recommendedItems,
                    isLandscape = false,
                    onItemFocused = { heroItem = it },
                    onItemClick = { item -> appManager.launchPlexMedia(item) }
                )
            }

            // 8. Films (Plex Movies Library)
            if (movieItems.isNotEmpty()) {
                MediaRow(
                    title = "Populaire Films",
                    items = movieItems,
                    isLandscape = false,
                    onItemFocused = { heroItem = it },
                    onItemClick = { item -> appManager.launchPlexMedia(item) }
                )
            }

            // 9. Series (Plex TV Series Library)
            if (seriesItems.isNotEmpty()) {
                MediaRow(
                    title = "Populaire Series",
                    items = seriesItems,
                    isLandscape = false,
                    onItemFocused = { heroItem = it },
                    onItemClick = { item -> appManager.launchPlexMedia(item) }
                )
            }

            Spacer(modifier = Modifier.height(70.dp))
        }

        // Search Dialog Overlay
        if (showSearchDialog) {
            SearchDialog(
                plexItems = allPlexItems,
                apps = apps,
                onSelectPlex = { item ->
                    showSearchDialog = false
                    appManager.launchPlexMedia(item)
                },
                onSelectApp = { app ->
                    showSearchDialog = false
                    appManager.launchApp(app.packageName)
                },
                onDismiss = { showSearchDialog = false }
            )
        }

        // TV Gids (EPG) Dialog Overlay
        if (showTvGuideDialog) {
            TvGuideDialog(
                channels = liveChannels,
                onSelectChannel = { channel ->
                    showTvGuideDialog = false
                    appManager.launchKpnTv("channel_${channel.channelNumber}")
                },
                onOpenNativeGuide = {
                    showTvGuideDialog = false
                    appManager.launchKpnTv("guide")
                },
                onDismiss = { showTvGuideDialog = false }
            )
        }

        // Server Status Dialog Overlay
        if (showStatusDialog) {
            StatusDialog(
                health = health,
                onDismiss = { showStatusDialog = false }
            )
        }

        // Settings Dialog Overlay
        if (showSettingsDialog) {
            SettingsDialog(
                configManager = configManager,
                onSaveAndReload = { loadContent() },
                onReturnToKpn = { appManager.launchKpnTv("home") },
                onOpenHomeSettings = { appManager.openHomeSettings() },
                onDismiss = { showSettingsDialog = false }
            )
        }
    }
}
