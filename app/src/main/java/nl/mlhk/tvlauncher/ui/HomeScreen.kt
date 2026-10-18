package nl.mlhk.tvlauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.mlhk.tvlauncher.data.AppManager
import nl.mlhk.tvlauncher.data.ConfigManager
import nl.mlhk.tvlauncher.data.PlexClient
import nl.mlhk.tvlauncher.data.StatusClient
import nl.mlhk.tvlauncher.model.AppItem
import nl.mlhk.tvlauncher.model.PlexMediaItem
import nl.mlhk.tvlauncher.model.SystemHealthResponse
import nl.mlhk.tvlauncher.ui.components.*
import nl.mlhk.tvlauncher.ui.theme.MlhkBackground
import nl.mlhk.tvlauncher.ui.theme.MlhkTextMuted
import nl.mlhk.tvlauncher.ui.theme.Typography

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

    var health by remember { mutableStateOf(SystemHealthResponse()) }
    var continueWatchingItems by remember { mutableStateOf<List<PlexMediaItem>>(emptyList()) }
    var recentlyAddedItems by remember { mutableStateOf<List<PlexMediaItem>>(emptyList()) }
    var recommendedItems by remember { mutableStateOf<List<PlexMediaItem>>(emptyList()) }
    var movieItems by remember { mutableStateOf<List<PlexMediaItem>>(emptyList()) }
    var seriesItems by remember { mutableStateOf<List<PlexMediaItem>>(emptyList()) }
    var apps by remember { mutableStateOf<List<AppItem>>(emptyList()) }

    var heroItem by remember { mutableStateOf<PlexMediaItem?>(null) }
    var isPlexLoading by remember { mutableStateOf(true) }

    fun loadContent() {
        coroutineScope.launch {
            isPlexLoading = true

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

            // Fetch Plex rows
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
                    heroItem = cw.firstOrNull() ?: rec.firstOrNull() ?: ra.firstOrNull()
                }
            } catch (e: Exception) {
                // Plex load error handled
            } finally {
                isPlexLoading = false
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
            } catch (e: Exception) {
                // ignore periodic status error
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MlhkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // 1. Top Navigation Bar (replaces KPN header with MLHK logo + tabs + status)
            TopNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                    when (tab) {
                        TopTab.LIVE_TV -> appManager.launchKpnTv("live")
                        TopTab.GUIDE -> appManager.launchKpnTv("guide")
                        TopTab.SAVED -> appManager.launchKpnTv("recordings")
                        TopTab.APPS -> {
                            coroutineScope.launch {
                                scrollState.animateScrollTo(600)
                            }
                        }
                        TopTab.MOVIES_SERIES -> {
                            coroutineScope.launch {
                                scrollState.animateScrollTo(900)
                            }
                        }
                        TopTab.START -> {
                            coroutineScope.launch {
                                scrollState.animateScrollTo(0)
                            }
                        }
                    }
                },
                health = health,
                onStatusClick = { showStatusDialog = true },
                onSettingsClick = { showSettingsDialog = true }
            )

            // 2. Hero Banner (Spotlight backdrop + active/top Plex item)
            HeroBanner(
                item = heroItem,
                onPlayClick = { item ->
                    appManager.launchPlexMedia(item)
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Verder kijken (Continue Watching from Plex with progress bar)
            if (continueWatchingItems.isNotEmpty()) {
                MediaRow(
                    title = "Verder kijken",
                    items = continueWatchingItems,
                    isLandscape = true,
                    onItemFocused = { heroItem = it },
                    onItemClick = { item -> appManager.launchPlexMedia(item) }
                )
            }

            // 4. Recent toegevoegd (Recently Added from Plex)
            if (recentlyAddedItems.isNotEmpty()) {
                MediaRow(
                    title = "Recent toegevoegd",
                    items = recentlyAddedItems,
                    isLandscape = false,
                    onItemFocused = { heroItem = it },
                    onItemClick = { item -> appManager.launchPlexMedia(item) }
                )
            }

            // 5. Aanbevolen voor jou (Recommended from Plex)
            if (recommendedItems.isNotEmpty()) {
                MediaRow(
                    title = "Aanbevolen voor jou",
                    items = recommendedItems,
                    isLandscape = false,
                    onItemFocused = { heroItem = it },
                    onItemClick = { item -> appManager.launchPlexMedia(item) }
                )
            }

            // 6. Favoriete Apps Row
            AppRow(
                title = "Favoriete Apps",
                apps = apps,
                onAppClick = { app ->
                    appManager.launchApp(app.packageName)
                }
            )

            // 7. Films (Plex Library)
            if (movieItems.isNotEmpty()) {
                MediaRow(
                    title = "Films",
                    items = movieItems,
                    isLandscape = false,
                    onItemFocused = { heroItem = it },
                    onItemClick = { item -> appManager.launchPlexMedia(item) }
                )
            }

            // 8. Series (Plex Library)
            if (seriesItems.isNotEmpty()) {
                MediaRow(
                    title = "Series",
                    items = seriesItems,
                    isLandscape = false,
                    onItemFocused = { heroItem = it },
                    onItemClick = { item -> appManager.launchPlexMedia(item) }
                )
            }

            // Fallback information if Plex has no items yet
            if (!isPlexLoading && continueWatchingItems.isEmpty() && recentlyAddedItems.isEmpty() && recommendedItems.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp, vertical = 24.dp)
                ) {
                    Text(
                        text = "Plex Media Server koppeling",
                        style = Typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Nog geen Plex-aanbevelingen geladen. Controleer via Instellingen (tandwiel rechtsboven) het Plex IP-adres en Token.",
                        style = Typography.bodyMedium.copy(color = MlhkTextMuted)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    TvButton(
                        text = "Instellingen openen",
                        isPrimary = true,
                        onClick = { showSettingsDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }

        // Status Dialog Overlay
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
