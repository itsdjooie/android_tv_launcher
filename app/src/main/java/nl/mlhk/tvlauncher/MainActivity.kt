package nl.mlhk.tvlauncher

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
        plexClient = PlexClient(configManager)
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
            // TV Gids button on Sagemcom DIW7022 remote
            KeyEvent.KEYCODE_GUIDE, KeyEvent.KEYCODE_TV_INPUT -> {
                appManager.launchKpnTv("guide")
                return true
            }
            // Channel Up / Channel Down buttons
            KeyEvent.KEYCODE_CHANNEL_UP, KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                appManager.launchKpnTv("channel")
                return true
            }
            // Return to top when back is pressed instead of closing launcher
            KeyEvent.KEYCODE_BACK -> {
                // Let user stay on launcher
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}
