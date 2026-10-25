package nl.mlhk.tvlauncher.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.mlhk.tvlauncher.data.AppManager
import nl.mlhk.tvlauncher.data.ConfigManager
import nl.mlhk.tvlauncher.data.ContinueWatchingProvider
import nl.mlhk.tvlauncher.data.LiveTvProvider
import nl.mlhk.tvlauncher.data.PlexClient
import nl.mlhk.tvlauncher.data.StatusClient
import nl.mlhk.tvlauncher.iptv.TvPlayerActivity
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val continueWatchingProvider = remember { ContinueWatchingProvider(appManager.context, appManager, configManager) }

    var selectedTab by remember { mutableStateOf(TopTab.START) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showFavoritesDialog by remember { mutableStateOf(false) }
    var showTvGuideDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }

    var health by remember { mutableStateOf(SystemHealthResponse()) }
    var liveChannels by remember { mutableStateOf<List<LiveTvChannel>>(emptyList()) }
    var unifiedContinueWatching by remember { mutableStateOf<List<ContinueWatchingItem>>(emptyList()) }

    var plexRecentlyAdded by remember { mutableStateOf<List<PlexMediaItem>>(PlexClient.cachedRecentlyAdded) }

    var allApps by remember { mutableStateOf<List<AppItem>>(emptyList()) }
    var favoriteApps by remember { mutableStateOf<List<AppItem>>(emptyList()) }
    var recentApps by remember { mutableStateOf<List<AppItem>>(emptyList()) }

    // 3 tot 5 Highlights (direct uit persistente schijf-cache, 0ms laadtijd!)
    var highlightItems by remember { mutableStateOf<List<PlexMediaItem>>(PlexClient.cachedHighlights) }

    var isPlexConfigured by remember { mutableStateOf(configManager.plexToken.isNotBlank()) }
    var isIptvConfigured by remember { mutableStateOf(configManager.isIptvConfigured) }

    fun playChannel(channelNumber: Int) {
        try {
            val intent = Intent(context, TvPlayerActivity::class.java).apply {
                putExtra("channel", (channelNumber - 1).coerceAtLeast(0))
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    // 0ms in-memory verversing bij app-klik zonder netwerkvertraging!
    fun refreshRecentApps() {
        favoriteApps = appManager.getFavoriteApps(allApps)
        recentApps = appManager.getRecentApps(allApps)
        unifiedContinueWatching = continueWatchingProvider.buildUnifiedList(allApps, configManager.favoritePackages)
    }

    fun loadContent() {
        // FASE 1: INTERFACE METEEN LADEN (0ms, direct met persistente schijf-cache)
        isIptvConfigured = configManager.isIptvConfigured
        isPlexConfigured = configManager.plexToken.isNotBlank()

        val apps = appManager.getInstalledApps()
        allApps = apps
        favoriteApps = appManager.getFavoriteApps(apps)
        recentApps = appManager.getRecentApps(apps)

        val favPackages = configManager.favoritePackages

        // Haal direct de schijf-cache op voor highlights en recent toegevoegd
        if (PlexClient.cachedHighlights.isNotEmpty()) {
            highlightItems = PlexClient.cachedHighlights
        }
        if (PlexClient.cachedRecentlyAdded.isNotEmpty()) {
            plexRecentlyAdded = PlexClient.cachedRecentlyAdded
        }

        unifiedContinueWatching = continueWatchingProvider.buildUnifiedList(apps, favPackages)

        // FASE 2: VERSE INFO ASYNCHROON OP ACHTERGROND OPHALEN (Alleen recent toegevoegd, 1 enkel licht verzoek!)
        coroutineScope.launch(Dispatchers.IO) {
            if (isPlexConfigured) {
                try {
                    val ra = plexClient.getRecentlyAdded()
                    withContext(Dispatchers.Main) {
                        plexRecentlyAdded = ra
                        val highlights = ra.take(5)
                        if (highlights.isNotEmpty()) {
                            highlightItems = highlights
                        }
                    }
                } catch (_: Exception) {}
            }

            // IPTV & Monitoring rustig in de achtergrond
            try {
                if (isIptvConfigured) {
                    val channels = LiveTvProvider.getChannels(configManager)
                    withContext(Dispatchers.Main) {
                        liveChannels = channels
                    }
                }
                if (configManager.monitoringApiUrl.isNotBlank()) {
                    val sysHealth = try {
                        statusClient.getSystemStatus()
                    } catch (_: Exception) {
                        SystemHealthResponse(status = "grey", message = "Monitoring offline")
                    }
                    withContext(Dispatchers.Main) {
                        health = sysHealth
                    }
                }
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        loadContent()
    }

    // Achtergrond 60s monitoring loop
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            try {
                health = statusClient.getSystemStatus()
            } catch (_: Exception) {}
        }
    }

    val allPlexItems = remember(plexRecentlyAdded) { plexRecentlyAdded }

    val liveTvRowActive = isIptvConfigured && liveChannels.isNotEmpty()
    val cwRowActive = unifiedContinueWatching.isNotEmpty()
    val recentAppsRowActive = recentApps.isNotEmpty()

    // Dynamische exacte indexen voor navigatie tabs
    val appsRowIndex = 2 + (if (liveTvRowActive) 1 else 0) + (if (cwRowActive) 1 else 0)
    val plexRecentRowIndex = appsRowIndex + 1 + (if (recentAppsRowActive) 1 else 0)

    // 1-Pass Ultra-efficiënte OLED Dark Background
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F1016),
                        Color(0xFF08080C),
                        Color(0xFF050507)
                    )
                )
            )
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 60.dp)
        ) {
            // 1. Top Navigation Bar
            item(key = "topNav") {
                TopNavigationBar(
                    selectedTab = selectedTab,
                    isIptvConfigured = isIptvConfigured,
                    onTabSelected = { tab ->
                        selectedTab = tab
                        when (tab) {
                            TopTab.START -> {
                                coroutineScope.launch { listState.animateScrollToItem(0) }
                            }
                            TopTab.LIVE_TV -> {
                                if (isIptvConfigured) {
                                    playChannel(1)
                                }
                            }
                            TopTab.GUIDE -> {
                                if (isIptvConfigured) {
                                    showTvGuideDialog = true
                                }
                            }
                            TopTab.MOVIES_SERIES -> {
                                coroutineScope.launch { listState.animateScrollToItem(plexRecentRowIndex.coerceAtLeast(0)) }
                            }
                            TopTab.APPS -> {
                                coroutineScope.launch { listState.animateScrollToItem(appsRowIndex.coerceAtLeast(0)) }
                            }
                        }
                    },
                    onSearchClick = { showSearchDialog = true },
                    health = health,
                    onStatusClick = { showStatusDialog = true },
                    onSettingsClick = { showSettingsDialog = true }
                )
            }

            // 2. Cinematic Hero Banner met 3 tot 5 Highlights (0ms laadtijd!)
            item(key = "hero") {
                if (isPlexConfigured) {
                    HeroBanner(
                        items = highlightItems,
                        onPlayClick = { item -> appManager.launchPlexMedia(item) }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
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
                                text = "Koppel via https://plex.tv/link om je eigen films, series en recent toegevoegde content hier te tonen.",
                                style = Typography.bodyMedium.copy(color = MlhkTextSub)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            TvButton(
                                text = "Nu Koppelen via Plex PIN",
                                isPrimary = true,
                                onClick = { showSettingsDialog = true }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 3. LIVE OP TV (Enkel zichtbaar als IPTV gekoppeld is! Geen KPN TV)
            if (isIptvConfigured && liveChannels.isNotEmpty()) {
                item(key = "liveTv") {
                    LiveTvRow(
                        title = "Live op TV (IPTV)",
                        channels = liveChannels,
                        onChannelClick = { channel ->
                            playChannel(channel.channelNumber)
                        }
                    )
                }
            }

            // 4. Verder kijken (Chronologisch gesorteerd op daadwerkelijk als LAATST bekeken/geopend!)
            if (unifiedContinueWatching.isNotEmpty()) {
                item(key = "continueWatching") {
                    ContinueWatchingRow(
                        title = "Verder kijken",
                        items = unifiedContinueWatching,
                        onItemClick = { item ->
                            if (item.plexItem != null) {
                                appManager.launchPlexMedia(item.plexItem)
                            } else {
                                appManager.launchApp(item.sourcePackage)
                                refreshRecentApps()
                            }
                        }
                    )
                }
            }

            // 5. Mijn favoriete apps (Zelf in te stellen met ruime padding eronder)
            item(key = "favoriteApps") {
                AppRow(
                    title = "Mijn favoriete apps",
                    apps = favoriteApps,
                    onAppClick = { app ->
                        appManager.launchApp(app.packageName)
                        refreshRecentApps()
                    },
                    onManageFavorites = { showFavoritesDialog = true }
                )
            }

            // 6. Recent gebruikte apps
            if (recentApps.isNotEmpty()) {
                item(key = "recentApps") {
                    AppRow(
                        title = "Recent gebruikte apps",
                        apps = recentApps,
                        onAppClick = { app ->
                            appManager.launchApp(app.packageName)
                            refreshRecentApps()
                        }
                    )
                }
            }

            // 7. Recent toegevoegd in Plex (ENIGE Plex bibliotheek-rij op het startscherm!)
            if (plexRecentlyAdded.isNotEmpty()) {
                item(key = "plexRecentlyAdded") {
                    MediaRow(
                        title = "Recent toegevoegd in Plex",
                        items = plexRecentlyAdded,
                        isLandscape = false,
                        onItemClick = { item -> appManager.launchPlexMedia(item) }
                    )
                }
            }
        }

        // Favorites Dialog Overlay
        if (showFavoritesDialog) {
            FavoritesDialog(
                allApps = allApps,
                configManager = configManager,
                onFavoritesUpdated = {
                    loadContent()
                },
                onDismiss = { showFavoritesDialog = false }
            )
        }

        // Search Dialog Overlay
        if (showSearchDialog) {
            SearchDialog(
                plexItems = allPlexItems,
                apps = allApps,
                onSelectPlex = { item ->
                    showSearchDialog = false
                    appManager.launchPlexMedia(item)
                },
                onSelectApp = { app ->
                    showSearchDialog = false
                    appManager.launchApp(app.packageName)
                    refreshRecentApps()
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
                    playChannel(channel.channelNumber)
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
                onOpenFavorites = { showFavoritesDialog = true },
                onOpenHomeSettings = { appManager.openHomeSettings() },
                onDismiss = { showSettingsDialog = false }
            )
        }
    }
}
