package nl.mlhk.tvlauncher.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import nl.mlhk.tvlauncher.model.AppItem
import nl.mlhk.tvlauncher.model.PlexMediaItem

class AppManager(val context: Context, private val configManager: ConfigManager) {

    private var cachedApps: List<AppItem>? = null

    fun getInstalledApps(forceRefresh: Boolean = false): List<AppItem> {
        if (!forceRefresh && cachedApps != null && cachedApps!!.isNotEmpty()) {
            return cachedApps!!
        }

        val pm = context.packageManager
        val selfPackage = context.packageName
        val appList = mutableListOf<AppItem>()
        val seenPackages = mutableSetOf<String>()

        // 1. Query Leanback TV launcher apps first
        val tvIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }
        val tvResolveInfos = pm.queryIntentActivities(tvIntent, PackageManager.MATCH_ALL)
        for (info in tvResolveInfos) {
            val pkg = info.activityInfo.packageName
            if (pkg == selfPackage || seenPackages.contains(pkg)) continue
            seenPackages.add(pkg)

            val label = info.loadLabel(pm).toString()
            val banner = runCatching { info.activityInfo.loadBanner(pm) }.getOrNull()
            val icon = runCatching { info.loadIcon(pm) }.getOrNull()
            val isKpn = pkg.contains("kpn", ignoreCase = true)
            val isPlex = pkg.contains("plex", ignoreCase = true)

            appList.add(
                AppItem(
                    packageName = pkg,
                    activityName = info.activityInfo.name,
                    label = label,
                    icon = icon,
                    banner = banner,
                    isKpn = isKpn,
                    isPlex = isPlex
                )
            )
        }

        // 2. Query general launcher apps as fallback
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val mainResolveInfos = pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL)
        for (info in mainResolveInfos) {
            val pkg = info.activityInfo.packageName
            if (pkg == selfPackage || seenPackages.contains(pkg)) continue
            seenPackages.add(pkg)

            val label = info.loadLabel(pm).toString()
            val banner = runCatching { info.activityInfo.loadBanner(pm) }.getOrNull()
            val icon = runCatching { info.loadIcon(pm) }.getOrNull()
            val isKpn = pkg.contains("kpn", ignoreCase = true)
            val isPlex = pkg.contains("plex", ignoreCase = true)

            appList.add(
                AppItem(
                    packageName = pkg,
                    activityName = info.activityInfo.name,
                    label = label,
                    icon = icon,
                    banner = banner,
                    isKpn = isKpn,
                    isPlex = isPlex
                )
            )
        }

        val sorted = appList.sortedWith(
            compareByDescending<AppItem> { it.isPlex }
                .thenByDescending { it.packageName.contains("videoland", ignoreCase = true) }
                .thenByDescending { it.packageName.contains("youtube", ignoreCase = true) }
                .thenBy { it.label.lowercase() }
        )
        cachedApps = sorted
        return sorted
    }

    fun getFavoriteApps(allApps: List<AppItem>): List<AppItem> {
        val favorites = configManager.favoritePackages
        val appMap = allApps.associateBy { it.packageName }
        val result = mutableListOf<AppItem>()
        for (pkg in favorites) {
            appMap[pkg]?.let { result.add(it) }
        }
        // Fallback: if no favorites match, show top installed streaming apps
        if (result.isEmpty()) {
            val defaults = listOf(
                "com.plexapp.android", "nl.rtl.videoland.v2", "com.wbd.hbomax",
                "com.disney.disneyplus", "app.revanced.android.youtube", "app.smarters.iptv"
            )
            for (pkg in defaults) {
                appMap[pkg]?.let { result.add(it) }
            }
            if (result.isEmpty()) {
                result.addAll(allApps.take(8))
            }
        }
        return result
    }

    fun getRecentApps(allApps: List<AppItem>): List<AppItem> {
        val recentPkgs = configManager.recentPackages
        val appMap = allApps.associateBy { it.packageName }
        val result = mutableListOf<AppItem>()
        for (pkg in recentPkgs) {
            appMap[pkg]?.let { result.add(it) }
        }
        return result.take(10)
    }

    fun launchApp(packageName: String): Boolean {
        return try {
            val pm = context.packageManager
            // 1. Android TV Leanback launch intent first!
            var intent = pm.getLeanbackLaunchIntentForPackage(packageName)
            // 2. Standard launch intent fallback
            if (intent == null) {
                intent = pm.getLaunchIntentForPackage(packageName)
            }
            // 3. Fallback: query activity directly
            if (intent == null) {
                val queryIntent = Intent(Intent.ACTION_MAIN).apply {
                    setPackage(packageName)
                    addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
                }
                val resolves = pm.queryIntentActivities(queryIntent, PackageManager.MATCH_ALL)
                if (resolves.isNotEmpty()) {
                    val act = resolves[0].activityInfo
                    intent = Intent(Intent.ACTION_MAIN).apply {
                        component = ComponentName(act.packageName, act.name)
                    }
                }
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                configManager.recordPackageUsage(packageName)
                true
            } else {
                Toast.makeText(context, "App niet gevonden: $packageName", Toast.LENGTH_SHORT).show()
                false
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Fout bij starten app: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun launchPlexMedia(item: PlexMediaItem): Boolean {
        val plexPkg = ConfigManager.PLEX_PACKAGE_NAME
        configManager.recordPackageUsage(plexPkg)

        // 1. Try direct deep-link URI with Plex server ID and metadata key
        try {
            val serverId = "695a0521970d6d3c3458b7275f754d743ade3a5a"
            val deepLinkUri = Uri.parse("plex://server/$serverId/com.plexapp.plugins.library/library/metadata/${item.ratingKey}")
            val deepIntent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
                setPackage(plexPkg)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (deepIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(deepIntent)
                return true
            }
        } catch (_: Exception) {}

        // 2. Try Leanback launch intent with playback extras
        try {
            val launchIntent = context.packageManager.getLeanbackLaunchIntentForPackage(plexPkg)
                ?: context.packageManager.getLaunchIntentForPackage(plexPkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                launchIntent.putExtra("key", item.key)
                launchIntent.putExtra("ratingKey", item.ratingKey)
                launchIntent.putExtra("type", item.type)
                context.startActivity(launchIntent)
                return true
            }
        } catch (_: Exception) {}

        return launchApp(plexPkg)
    }

    fun openAndroidSettings() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Instellingen niet bereikbaar", Toast.LENGTH_SHORT).show()
        }
    }

    fun openHomeSettings() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            openAndroidSettings()
        }
    }
}
