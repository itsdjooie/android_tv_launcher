package nl.mlhk.tvlauncher.iptv

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.KeyEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import nl.mlhk.tvlauncher.data.ConfigManager

/** Native MediaPlayer IPTV player activity for Android TV */
class TvPlayerActivity : Activity(), SurfaceHolder.Callback {
    private var player: MediaPlayer? = null
    private lateinit var surface: SurfaceView
    private lateinit var title: TextView
    private lateinit var audio: AudioManager
    private lateinit var focus: AudioFocusRequest
    private val handler = Handler(Looper.getMainLooper())
    private var index = 0
    private var resumed = false
    private var ready = false
    private lateinit var configManager: ConfigManager
    private var dialog: AlertDialog? = null
    private var digits = ""
    private val selectNumber = Runnable {
        val number = digits.toIntOrNull()
        digits = ""
        val count = TvSession.playbackChannels.ifEmpty { TvSession.catalog?.channels.orEmpty() }.size
        if (number != null && number in 1..count) {
            index = number - 1
            tune()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        configManager = ConfigManager(this)
        index = savedInstanceState?.getInt("channel") ?: intent.getIntExtra("channel", 0)

        val frame = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        surface = SurfaceView(this)
        surface.holder.addCallback(this)
        frame.addView(surface, FrameLayout.LayoutParams(-1, -1, Gravity.CENTER))

        title = TextView(this).apply {
            setTextColor(Color.WHITE)
            textSize = 22f
            setPadding(48, 28, 48, 28)
            setBackgroundColor(0xCC080712.toInt())
        }
        frame.addView(title, FrameLayout.LayoutParams(-1, -2, Gravity.TOP))
        setContentView(frame)

        audio = getSystemService(AudioManager::class.java)
        focus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .build()
            )
            .setOnAudioFocusChangeListener { change ->
                if (change < 0) player?.pause() else if (change == AudioManager.AUDIOFOCUS_GAIN) player?.start()
            }.build()

        if (TvSession.catalog == null && TvSession.playbackChannels.isEmpty()) {
            title.text = "Open de zender opnieuw vanuit MLHK TV."
            handler.postDelayed({ finish() }, 2000)
        }
    }

    override fun onResume() {
        super.onResume()
        resumed = true
        if (ready) tune()
    }

    override fun onPause() {
        resumed = false
        releasePlayer()
        super.onPause()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        dialog?.dismiss()
        releasePlayer()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("channel", index)
        super.onSaveInstanceState(outState)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        ready = true
        if (resumed) tune()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        ready = false
        releasePlayer()
    }

    private fun releasePlayer() {
        handler.removeCallbacksAndMessages(null)
        player?.release()
        player = null
        audio.abandonAudioFocusRequest(focus)
    }

    private fun tune() {
        releasePlayer()
        val channels = TvSession.playbackChannels.ifEmpty { TvSession.catalog?.channels.orEmpty() }
        if (channels.isEmpty()) return
        index = Math.floorMod(index, channels.size)
        val channel = channels[index]
        title.text = "${index + 1}.  ${channel.name}"
        title.visibility = View.VISIBLE

        if (!IptvRepository.validUrl(channel.url, configManager.iptvAllowHttp)) {
            unavailable()
            return
        }
        if (audio.requestAudioFocus(focus) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            unavailable()
            return
        }
        try {
            val media = MediaPlayer()
            player = media
            var prepared = false
            media.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .build()
            )
            media.setDisplay(surface.holder)
            media.setOnPreparedListener {
                prepared = true
                if (player === it && resumed) {
                    it.start()
                    handler.postDelayed({ title.visibility = View.GONE }, 3500)
                }
            }
            media.setOnErrorListener { failed, _, _ ->
                if (player === failed) unavailable()
                true
            }
            media.setOnVideoSizeChangedListener { _, width, height ->
                if (width > 0 && height > 0) {
                    val available = android.graphics.Point()
                    @Suppress("DEPRECATION")
                    windowManager.defaultDisplay.getSize(available)
                    val ratio = minOf(available.x.toFloat() / width, available.y.toFloat() / height)
                    surface.layoutParams = FrameLayout.LayoutParams((width * ratio).toInt(), (height * ratio).toInt(), Gravity.CENTER)
                }
            }
            media.setDataSource(this, Uri.parse(channel.url), mapOf("User-Agent" to "MLHK-TV/1.0"))
            media.prepareAsync()
            handler.postDelayed({ if (player === media && !prepared) unavailable() }, 15000)
        } catch (_: Exception) {
            unavailable()
        }
    }

    private fun unavailable() {
        releasePlayer()
        if (isFinishing || dialog?.isShowing == true) return
        dialog = AlertDialog.Builder(this)
            .setTitle("Zender niet beschikbaar")
            .setMessage("Controleer de providerverbinding of kies een andere zender.")
            .setPositiveButton("Opnieuw") { _, _ -> tune() }
            .setNeutralButton("Volgende zender") { _, _ -> index++; tune() }
            .setNegativeButton("Terug") { _, _ -> finish() }
            .show()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_CHANNEL_UP -> {
                index++
                tune()
                return true
            }
            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                index--
                tune()
                return true
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                title.visibility = if (title.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                return true
            }
            KeyEvent.KEYCODE_BACK -> {
                finish()
                return true
            }
            in KeyEvent.KEYCODE_0..KeyEvent.KEYCODE_9 -> {
                digits += (keyCode - KeyEvent.KEYCODE_0).toString()
                title.text = "Zender: $digits"
                title.visibility = View.VISIBLE
                handler.removeCallbacks(selectNumber)
                handler.postDelayed(selectNumber, 2000)
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}
