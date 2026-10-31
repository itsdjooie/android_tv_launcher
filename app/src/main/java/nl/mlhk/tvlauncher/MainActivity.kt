package nl.mlhk.tvlauncher

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import nl.mlhk.tvlauncher.data.AppManager
import nl.mlhk.tvlauncher.data.ConfigManager
import nl.mlhk.tvlauncher.data.PlexClient
import nl.mlhk.tvlauncher.data.StatusClient
import nl.mlhk.tvlauncher.iptv.TvPlayerActivity
import nl.mlhk.tvlauncher.ui.HomeScreen
import nl.mlhk.tvlauncher.ui.theme.MLHKTheme

class MainActivity : ComponentActivity() {

    private lateinit var configManager: ConfigManager
    private lateinit var plexClient: PlexClient
    private lateinit var statusClient: StatusClient
    private lateinit var appManager: AppManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configManager = ConfigManager(this)
        plexClient = PlexClient(this, configManager)
        statusClient = StatusClient(configManager)
        appManager = AppManager(this, configManager)

        setContent {
            MLHKTheme {
                HomeScreen(
                    configManager = configManager,
                    plexClient = plexClient,
                    statusClient = statusClient,
                    appManager = appManager,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_HOME -> {
                event?.startTracking()
                return false
            }
            KeyEvent.KEYCODE_GUIDE, KeyEvent.KEYCODE_TV_INPUT -> {
                if (configManager.isIptvConfigured) {
                    val intent = Intent(this, TvPlayerActivity::class.java).apply {
                        putExtra("channel", 0)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                    return true
                }
            }
            KeyEvent.KEYCODE_CHANNEL_UP, KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                if (configManager.isIptvConfigured) {
                    val intent = Intent(this, TvPlayerActivity::class.java).apply {
                        putExtra("channel", 0)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                    return true
                }
            }
            KeyEvent.KEYCODE_BACK -> {
                // Keep user safely on launcher
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            val fallbackLaunchers = listOf(
                "com.google.android.apps.tv.launcherx", // Google TV (Chromecast with Google TV)
                "com.kpn.tvlauncher",                  // KPN TV+ Box (Sagemcom DIW7022)
                "com.google.android.tvlauncher"         // Android TV Leanback
            )
            for (pkg in fallbackLaunchers) {
                try {
                    val fallbackIntent = packageManager.getLaunchIntentForPackage(pkg)
                    if (fallbackIntent != null) {
                        fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(fallbackIntent)
                        return true
                    }
                } catch (_: Exception) {}
            }
        }
        return super.onKeyLongPress(keyCode, event)
    }
}
