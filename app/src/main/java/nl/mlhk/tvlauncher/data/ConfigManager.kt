package nl.mlhk.tvlauncher.data

import android.content.Context
import android.content.SharedPreferences

class ConfigManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var plexHost: String
        get() = prefs.getString(KEY_PLEX_HOST, DEFAULT_PLEX_HOST) ?: DEFAULT_PLEX_HOST
        set(value) = prefs.edit().putString(KEY_PLEX_HOST, value.trim()).apply()

    var plexToken: String
        get() = prefs.getString(KEY_PLEX_TOKEN, DEFAULT_PLEX_TOKEN) ?: DEFAULT_PLEX_TOKEN
        set(value) = prefs.edit().putString(KEY_PLEX_TOKEN, value.trim()).apply()

    var monitoringApiUrl: String
        get() = prefs.getString(KEY_MONITORING_URL, DEFAULT_MONITORING_URL) ?: DEFAULT_MONITORING_URL
        set(value) = prefs.edit().putString(KEY_MONITORING_URL, value.trim()).apply()

    var monitoringApiKey: String
        get() = prefs.getString(KEY_MONITORING_KEY, DEFAULT_MONITORING_KEY) ?: DEFAULT_MONITORING_KEY
        set(value) = prefs.edit().putString(KEY_MONITORING_KEY, value.trim()).apply()

    var kpnPackageName: String
        get() = prefs.getString(KEY_KPN_PACKAGE, DEFAULT_KPN_PACKAGE) ?: DEFAULT_KPN_PACKAGE
        set(value) = prefs.edit().putString(KEY_KPN_PACKAGE, value.trim()).apply()

    companion object {
        private const val PREFS_NAME = "mlhk_launcher_config"

        private const val KEY_PLEX_HOST = "plex_host"
        private const val KEY_PLEX_TOKEN = "plex_token"
        private const val KEY_MONITORING_URL = "monitoring_url"
        private const val KEY_MONITORING_KEY = "monitoring_key"
        private const val KEY_KPN_PACKAGE = "kpn_package"

        // Sensible defaults for MLHK local network
        const val DEFAULT_PLEX_HOST = "https://192.168.2.25:32400"
        const val DEFAULT_PLEX_TOKEN = ""
        const val DEFAULT_MONITORING_URL = "http://192.168.2.25:8080/api/status"
        const val DEFAULT_MONITORING_KEY = "mlhk-secure-key"
        const val DEFAULT_KPN_PACKAGE = "com.kpn.tvlauncher"
        const val PLEX_PACKAGE_NAME = "com.plexapp.android"
    }
}
