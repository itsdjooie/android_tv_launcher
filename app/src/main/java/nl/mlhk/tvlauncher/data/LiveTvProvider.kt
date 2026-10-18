package nl.mlhk.tvlauncher.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.mlhk.tvlauncher.iptv.Channel
import nl.mlhk.tvlauncher.iptv.IptvRepository
import nl.mlhk.tvlauncher.iptv.TvCatalog
import nl.mlhk.tvlauncher.iptv.TvOrganizer
import nl.mlhk.tvlauncher.iptv.TvSession
import nl.mlhk.tvlauncher.model.LiveTvChannel
import java.text.SimpleDateFormat
import java.util.*

object LiveTvProvider {

    suspend fun getChannels(configManager: ConfigManager): List<LiveTvChannel> = withContext(Dispatchers.IO) {
        // Enkel zichtbaar als IPTV gekoppeld is! Geen KPN TV.
        if (!configManager.isIptvConfigured) {
            TvSession.catalog = null
            TvSession.playbackChannels = emptyList()
            return@withContext emptyList()
        }

        try {
            val repo = IptvRepository(
                iptvType = configManager.iptvType,
                iptvServer = configManager.iptvServer,
                iptvUsername = configManager.iptvUsername,
                iptvPassword = configManager.iptvPassword,
                iptvM3u = configManager.iptvM3uUrl,
                iptvXmltv = configManager.iptvEpgUrl,
                allowHttp = configManager.iptvAllowHttp
            )

            val catalog = repo.load()
            TvSession.catalog = catalog

            val families = TvOrganizer.families(catalog.channels)
            TvSession.families = families

            val playbackList = families.map { it.primary }
            TvSession.playbackChannels = playbackList

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val now = System.currentTimeMillis()

            val result = mutableListOf<LiveTvChannel>()
            playbackList.take(60).forEachIndexed { index, channel ->
                val currentProg = catalog.now(channel, now)
                val nextProg = catalog.next(channel, now)

                val progTitle = currentProg?.title?.ifBlank { null } ?: "Live uitzending"
                val nextTitle = nextProg?.title?.ifBlank { null } ?: "Volgend programma"

                val timeSlot = if (currentProg != null) {
                    "${timeFormat.format(Date(currentProg.start))} - ${timeFormat.format(Date(currentProg.end))}"
                } else {
                    ""
                }

                val progress = if (currentProg != null && currentProg.end > currentProg.start) {
                    ((now - currentProg.start).toFloat() / (currentProg.end - currentProg.start).toFloat()).coerceIn(0f, 1f)
                } else {
                    0.5f
                }

                result.add(
                    LiveTvChannel(
                        channelNumber = index + 1,
                        name = channel.name,
                        currentProgram = progTitle,
                        nextProgram = nextTitle,
                        timeSlot = timeSlot,
                        progress = progress,
                        category = channel.group.ifBlank { "Algemeen" },
                        logoText = channel.name.take(6).uppercase()
                    )
                )
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }
}
