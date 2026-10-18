package nl.mlhk.tvlauncher.data

import android.content.Context
import android.content.SharedPreferences

class ConfigManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // --- PLEX CONFIG ---
    var plexHost: String
        get() = prefs.getString(KEY_PLEX_HOST, DEFAULT_PLEX_HOST) ?: DEFAULT_PLEX_HOST
        set(value) = prefs.edit().putString(KEY_PLEX_HOST, value.trim()).apply()

    var plexToken: String
        get() = prefs.getString(KEY_PLEX_TOKEN, DEFAULT_PLEX_TOKEN) ?: DEFAULT_PLEX_TOKEN
        set(value) = prefs.edit().putString(KEY_PLEX_TOKEN, value.trim()).apply()

    // --- MONITORING CONFIG ---
    var monitoringApiUrl: String
        get() = prefs.getString(KEY_MONITORING_URL, DEFAULT_MONITORING_URL) ?: DEFAULT_MONITORING_URL
        set(value) = prefs.edit().putString(KEY_MONITORING_URL, value.trim()).apply()

    var monitoringApiKey: String
        get() = prefs.getString(KEY_MONITORING_KEY, DEFAULT_MONITORING_KEY) ?: DEFAULT_MONITORING_KEY
        set(value) = prefs.edit().putString(KEY_MONITORING_KEY, value.trim()).apply()

    // --- IPTV CONFIG ---
    var iptvType: String
        get() = prefs.getString(KEY_IPTV_TYPE, "none") ?: "none"
        set(value) = prefs.edit().putString(KEY_IPTV_TYPE, value.trim()).apply()

    var iptvServer: String
        get() = prefs.getString(KEY_IPTV_SERVER, "") ?: ""
        set(value) = prefs.edit().putString(KEY_IPTV_SERVER, value.trim()).apply()

    var iptvUsername: String
        get() = prefs.getString(KEY_IPTV_USERNAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_IPTV_USERNAME, value.trim()).apply()

    var iptvPassword: String
        get() = prefs.getString(KEY_IPTV_PASSWORD, "") ?: ""
        set(value) = prefs.edit().putString(KEY_IPTV_PASSWORD, value.trim()).apply()

    var iptvM3uUrl: String
        get() = prefs.getString(KEY_IPTV_M3U_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_IPTV_M3U_URL, value.trim()).apply()

    var iptvEpgUrl: String
        get() = prefs.getString(KEY_IPTV_EPG_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_IPTV_EPG_URL, value.trim()).apply()

    var iptvAllowHttp: Boolean
        get() = prefs.getBoolean(KEY_IPTV_ALLOW_HTTP, false)
        set(value) = prefs.edit().putBoolean(KEY_IPTV_ALLOW_HTTP, value).apply()

    val isIptvConfigured: Boolean
        get() = when (iptvType) {
            "xtream" -> iptvServer.isNotBlank() && iptvUsername.isNotBlank()
            "m3u" -> iptvM3uUrl.isNotBlank()
            else -> false
        }

    // --- FAVORITE APPS CONFIG ---
    var favoritePackages: Set<String>
        get() = prefs.getStringSet(KEY_FAVORITE_PACKAGES, null) ?: DEFAULT_FAVORITES
        set(value) = prefs.edit().putStringSet(KEY_FAVORITE_PACKAGES, value).apply()

    fun setFavorites(packages: Collection<String>) {
        favoritePackages = packages.toSet()
    }

    fun isFavorite(pkg: String): Boolean {
        return favoritePackages.contains(pkg)
    }

    fun toggleFavorite(pkg: String) {
        val current = favoritePackages.toMutableSet()
        if (current.contains(pkg)) {
            current.remove(pkg)
        } else {
            current.add(pkg)
        }
        favoritePackages = current
    }

    // --- RECENT APPS CONFIG ---
    val recentPackages: List<String>
        get() {
            val raw = prefs.getString(KEY_RECENT_PACKAGES, "") ?: ""
            if (raw.isBlank()) return emptyList()
            return raw.split(",").filter { it.isNotBlank() }
        }

    fun addRecentPackage(pkg: String) {
        if (pkg.isBlank()) return
        val current = recentPackages.toMutableList()
        current.remove(pkg)
        current.add(0, pkg)
        val trimmed = current.take(12)
        prefs.edit().putString(KEY_RECENT_PACKAGES, trimmed.joinToString(",")).apply()
    }

    fun getPackageLastUsedTime(pkg: String): Long {
        val stored = prefs.getLong("pkg_last_used_$pkg", 0L)
        if (stored > 0L) return stored
        val idx = recentPackages.indexOf(pkg)
        return if (idx >= 0) {
            System.currentTimeMillis() - (idx * 1800_000L)
        } else {
            0L
        }
    }

    fun recordPackageUsage(pkg: String) {
        if (pkg.isBlank()) return
        addRecentPackage(pkg)
        prefs.edit().putLong("pkg_last_used_$pkg", System.currentTimeMillis()).apply()
    }

    companion object {
        private const val PREFS_NAME = "mlhk_launcher_config"

        private const val KEY_PLEX_HOST = "plex_host"
        private const val KEY_PLEX_TOKEN = "plex_token"
        private const val KEY_MONITORING_URL = "monitoring_url"
        private const val KEY_MONITORING_KEY = "monitoring_key"

        private const val KEY_IPTV_TYPE = "iptv_type"
        private const val KEY_IPTV_SERVER = "iptv_server"
        private const val KEY_IPTV_USERNAME = "iptv_username"
        private const val KEY_IPTV_PASSWORD = "iptv_password"
        private const val KEY_IPTV_M3U_URL = "iptv_m3u_url"
        private const val KEY_IPTV_EPG_URL = "iptv_epg_url"
        private const val KEY_IPTV_ALLOW_HTTP = "iptv_allow_http"

        private const val KEY_FAVORITE_PACKAGES = "favorite_packages"
        private const val KEY_RECENT_PACKAGES = "recent_packages"

        const val DEFAULT_PLEX_HOST = "https://plex.mlhk.nl"
        const val DEFAULT_PLEX_TOKEN = "X76tt4b-vVQD-oThM18A"
        const val DEFAULT_MONITORING_URL = "https://private.mlhk.nl/api/tv-status"
        const val DEFAULT_MONITORING_KEY = "ad1afce0957a8c41b2a288cd42d4f28cdebb0253eac8dd2ec4d8787fe8610873"
        const val PLEX_PACKAGE_NAME = "com.plexapp.android"

        val DEFAULT_FAVORITES = setOf(
            "com.plexapp.android",
            "nl.rtl.videoland.v2",
            "com.wbd.hbomax",
            "com.disney.disneyplus",
            "app.revanced.android.youtube",
            "app.smarters.iptv",
            "com.spotify.tv.android",
            "org.xbmc.kodi",
            "org.jellyfin.androidtv"
        )
    }
}
