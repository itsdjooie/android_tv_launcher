package nl.mlhk.tvlauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.mlhk.tvlauncher.data.AppManager
import nl.mlhk.tvlauncher.data.ConfigManager
import nl.mlhk.tvlauncher.data.ContinueWatchingProvider
import nl.mlhk.tvlauncher.data.LiveTvProvider
import nl.mlhk.tvlauncher.data.PlexClient
import nl.mlhk.tvlauncher.data.StatusClient
import nl.mlhk.tvlauncher.model.AppItem
import nl.mlhk.tvlauncher.model.ContinueWatchingItem
import nl.mlhk.tvlauncher.model.LiveTvChannel
import nl.mlhk.tvlauncher.model.PlexMediaItem
import nl.mlhk.tvlauncher.model.SystemHealthResponse
import nl.mlhk.tvlauncher.ui.components.*
import nl.mlhk.tvlauncher.ui.theme.*

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
    val continueWatchingProvider = remember { ContinueWatchingProvider(appManager.context, appManager) }

    var selectedTab by remember { mutableStateOf(TopTab.START) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showTvGuideDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }

    var health by remember { mutableStateOf(SystemHealthResponse()) }
    var liveChannels by remember { mutableStateOf<List<LiveTvChannel>>(emptyList()) }
    var unifiedContinueWatching by remember { mutableStateOf<List<ContinueWatchingItem>>(emptyList()) }

    var plexContinueWatching by remember { mutableStateOf<List<PlexMediaItem>>(emptyList()) }
    var plexRecentlyAdded by remember { mutableStateOf<List<PlexMediaItem>>(emptyList()) }
    var plexRecommended by remember { mutableStateOf<List<PlexMediaItem>>(emptyList()) }
    var plexMovies by remember { mutableStateOf<List<PlexMediaItem>>(emptyList()) }
    var plexSeries by remember { mutableStateOf<List<PlexMediaItem>>(emptyList()) }

    var apps by remember { mutableStateOf<List<AppItem>>(emptyList()) }
    var heroItem by remember { mutableStateOf<PlexMediaItem?>(null) }
    var isPlexConfigured by remember { mutableStateOf(configManager.plexToken.isNotBlank()) }

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

            isPlexConfigured = configManager.plexToken.isNotBlank()

            // Fetch real Plex content
            if (isPlexConfigured) {
                try {
                    val cw = plexClient.getContinueWatching()
                    val ra = plexClient.getRecentlyAdded()
                    val rec = plexClient.getRecommended()
                    val mov = plexClient.getMovies()
                    val ser = plexClient.getSeries()

                    plexContinueWatching = cw
                    plexRecentlyAdded = ra
                    plexRecommended = rec
                    plexMovies = mov
                    plexSeries = ser

                    heroItem = ra.firstOrNull() ?: rec.firstOrNull() ?: cw.firstOrNull()
                } catch (e: Exception) {
                    // error handled
                }
            } else {
                plexContinueWatching = emptyList()
                plexRecentlyAdded = emptyList()
                plexRecommended = emptyList()
                plexMovies = emptyList()
                plexSeries = emptyList()
                heroItem = null
            }

            // Build unified Continue Watching list (combining Plex + Videoland + Netflix + Prime)
            unifiedContinueWatching = continueWatchingProvider.buildUnifiedList(
                plexItems = plexContinueWatching,
                installedApps = apps
            )
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
                liveChannels = LiveTvProvider.getChannels()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    val allPlexItems = remember(plexContinueWatching, plexRecentlyAdded, plexRecommended, plexMovies, plexSeries) {
        (plexContinueWatching + plexRecentlyAdded + plexRecommended + plexMovies + plexSeries).distinctBy { it.id }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MlhkBackground)
    ) {
        // MLHK Next-Gen Ambient Aurora Background
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

            // 2. Cinematic Hero Banner (Mainly Plex Focus)
            if (heroItem != null) {
                HeroBanner(
                    item = heroItem,
                    onPlayClick = { item -> appManager.launchPlexMedia(item) }
                )
            } else {
                // Calm prompt to link Plex via PIN
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .padding(horizontal = 48.dp, vertical = 20.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MlhkCardBg)
                        .border(1.dp, MlhkCardBorderGlow, RoundedCornerShape(20.dp))
                        .padding(28.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "KOPPEL JE PLEX MEDIA SERVER",
                            style = Typography.labelMedium.copy(color = MlhkNeonPink, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Koppel met 1-klik via Plex PIN",
                            style = Typography.displayLarge.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Server gedetecteerd op https://192.168.2.25:32400. Koppel via PIN om je eigen films, series en recent toegevoegde content hier te tonen.",
                            style = Typography.bodyMedium.copy(color = MlhkTextSub)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TvButton(
                            text = "Nu Koppelen via Plex PIN (1-klik)",
                            isPrimary = true,
                            onClick = { showSettingsDialog = true }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Nu op TV (Live Zenders Row - KPN TV Feature)
            LiveTvRow(
                title = "Nu op TV (Live Zenders)",
                channels = liveChannels,
                onChannelClick = { channel ->
                    appManager.launchKpnTv("channel_${channel.channelNumber}")
                }
            )

            // 4. Verder kijken (Unified: Plex + Videoland + Netflix + Prime)
            if (unifiedContinueWatching.isNotEmpty()) {
                ContinueWatchingRow(
                    title = "Verder kijken",
                    items = unifiedContinueWatching,
                    onItemClick = { item ->
                        if (item.plexItem != null) {
                            appManager.launchPlexMedia(item.plexItem)
                        } else {
                            appManager.launchApp(item.sourcePackage)
                        }
                    }
                )
            }

            // 5. Mijn favoriete apps Row (Exact KPN DIW7022 16:9 cards)
            AppRow(
                title = "Mijn favoriete apps",
                apps = apps,
                onAppClick = { app -> appManager.launchApp(app.packageName) }
            )

            // 6. Recent toegevoegd in Plex (100% Plex Focus)
            if (plexRecentlyAdded.isNotEmpty()) {
                MediaRow(
                    title = "Recent toegevoegd in Plex",
                    items = plexRecentlyAdded,
                    isLandscape = false,
                    onItemFocused = { heroItem = it },
                    onItemClick = { item -> appManager.launchPlexMedia(item) }
                )
            }

            // 7. Aanbevolen voor jou (100% Plex Focus)
            if (plexRecommended.isNotEmpty()) {
                MediaRow(
                    title = "Aanbevolen in Plex",
                    items = plexRecommended,
                    isLandscape = false,
                    onItemFocused = { heroItem = it },
                    onItemClick = { item -> appManager.launchPlexMedia(item) }
                )
            }

            // 8. Films (Plex Movies Library)
            if (plexMovies.isNotEmpty()) {
                MediaRow(
                    title = "Plex Films",
                    items = plexMovies,
                    isLandscape = false,
                    onItemFocused = { heroItem = it },
                    onItemClick = { item -> appManager.launchPlexMedia(item) }
                )
            }

            // 9. Series (Plex TV Series Library)
            if (plexSeries.isNotEmpty()) {
                MediaRow(
                    title = "Plex Series",
                    items = plexSeries,
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
                plexClient = plexClient,
                onSaveAndReload = { loadContent() },
                onReturnToKpn = { appManager.launchKpnTv("home") },
                onOpenHomeSettings = { appManager.openHomeSettings() },
                onDismiss = { showSettingsDialog = false }
            )
        }
    }
}
