package nl.mlhk.tvlauncher.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import nl.mlhk.tvlauncher.model.AppItem
import nl.mlhk.tvlauncher.model.PlexMediaItem

class AppManager(private val context: Context, private val configManager: ConfigManager) {

    fun getInstalledApps(): List<AppItem> {
        val pm = context.packageManager
        val selfPackage = context.packageName
        val appList = mutableListOf<AppItem>()
        val seenPackages = mutableSetOf<String>()

        // 1. Query Leanback TV launcher apps
        val tvIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }
        val tvResolveInfos = pm.queryIntentActivities(tvIntent, PackageManager.MATCH_ALL)
        for (info in tvResolveInfos) {
            val pkg = info.activityInfo.packageName
            if (pkg == selfPackage || seenPackages.contains(pkg)) continue
            seenPackages.add(pkg)

            val label = info.loadLabel(pm).toString()
            val icon = info.loadIcon(pm)
            val isKpn = pkg.contains("kpn", ignoreCase = true)
            val isPlex = pkg.contains("plex", ignoreCase = true)

            appList.add(
                AppItem(
                    packageName = pkg,
                    activityName = info.activityInfo.name,
                    label = label,
                    icon = icon,
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
            val icon = info.loadIcon(pm)
            val isKpn = pkg.contains("kpn", ignoreCase = true)
            val isPlex = pkg.contains("plex", ignoreCase = true)

            appList.add(
                AppItem(
                    packageName = pkg,
                    activityName = info.activityInfo.name,
                    label = label,
                    icon = icon,
                    isKpn = isKpn,
                    isPlex = isPlex
                )
            )
        }

        // Sort: Priority apps (Plex, KPN, YouTube, Netflix) first, then alphabetical
        return appList.sortedWith(
            compareByDescending<AppItem> { it.isPlex }
                .thenByDescending { it.isKpn }
                .thenByDescending { it.packageName.contains("youtube", ignoreCase = true) }
                .thenByDescending { it.packageName.contains("netflix", ignoreCase = true) }
                .thenBy { it.label.lowercase() }
        )
    }

    fun launchApp(packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
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

    fun launchKpnTv(destination: String = "home"): Boolean {
        val kpnPkg = configManager.kpnPackageName
        val launched = launchApp(kpnPkg)
        if (!launched) {
            // Also try alternative known KPN package names
            val alternatives = listOf("com.kpn.kpnandroidtv", "nl.kpn.itv.androidtv", "com.kpn.tvplus")
            for (alt in alternatives) {
                if (alt != kpnPkg && launchApp(alt)) return true
            }
            Toast.makeText(context, "KPN TV+ app niet geïnstalleerd of bereikbaar", Toast.LENGTH_LONG).show()
        }
        return launched
    }

    fun launchPlexMedia(item: PlexMediaItem): Boolean {
        val plexPkg = ConfigManager.PLEX_PACKAGE_NAME

        // Try direct deep-link URI first
        try {
            val deepLinkUri = Uri.parse("plex://server/?key=${Uri.encode(item.key)}")
            val deepIntent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
                setPackage(plexPkg)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (deepIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(deepIntent)
                return true
            }
        } catch (e: Exception) {
            // deep link fallback
        }

        // Try launching Plex app with extras
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(plexPkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                launchIntent.putExtra("key", item.key)
                launchIntent.putExtra("ratingKey", item.ratingKey)
                launchIntent.putExtra("type", item.type)
                context.startActivity(launchIntent)
                return true
            }
        } catch (e: Exception) {
            // fallback
        }

        Toast.makeText(context, "Plex Android TV app niet gevonden. Start '${item.title}' via Plex.", Toast.LENGTH_LONG).show()
        return false
    }

    fun openAndroidSettings() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Instellingen niet bereikbaar", Toast.LENGTH_SHORT).show()
        }
    }

    fun openHomeSettings() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            openAndroidSettings()
        }
    }
}
