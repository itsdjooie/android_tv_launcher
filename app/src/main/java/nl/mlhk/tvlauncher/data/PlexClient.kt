package nl.mlhk.tvlauncher.data

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.mlhk.tvlauncher.model.PlexMediaItem
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class PlexClient(private val configManager: ConfigManager) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    suspend fun getContinueWatching(): List<PlexMediaItem> = withContext(Dispatchers.IO) {
        val host = configManager.plexHost
        val token = configManager.plexToken

        if (host.isNotBlank()) {
            val endpoints = listOf("/hubs/continueWatching", "/library/onDeck")
            for (endpoint in endpoints) {
                val items = fetchItemsFromEndpoint(host, token, endpoint)
                if (items.isNotEmpty()) return@withContext items
            }
        }
        // Curated high-res fallback items matching real Plex Continue Watching
        getSampleContinueWatching()
    }

    suspend fun getRecentlyAdded(): List<PlexMediaItem> = withContext(Dispatchers.IO) {
        val host = configManager.plexHost
        val token = configManager.plexToken

        if (host.isNotBlank()) {
            val endpoints = listOf("/hubs/recentlyAdded", "/library/recentlyAdded")
            for (endpoint in endpoints) {
                val items = fetchItemsFromEndpoint(host, token, endpoint)
                if (items.isNotEmpty()) return@withContext items
            }
        }
        getSampleRecentlyAdded()
    }

    suspend fun getRecommended(): List<PlexMediaItem> = withContext(Dispatchers.IO) {
        val host = configManager.plexHost
        val token = configManager.plexToken

        if (host.isNotBlank()) {
            val items = fetchItemsFromEndpoint(host, token, "/hubs/promoted")
            if (items.isNotEmpty()) return@withContext items
            val fallback = fetchItemsFromEndpoint(host, token, "/library/all")
            if (fallback.isNotEmpty()) return@withContext fallback
        }
        getSampleRecommended()
    }

    suspend fun getMovies(): List<PlexMediaItem> = withContext(Dispatchers.IO) {
        val host = configManager.plexHost
        val token = configManager.plexToken
        if (host.isNotBlank()) {
            val items = fetchItemsFromEndpoint(host, token, "/library/sections/1/all")
            if (items.isNotEmpty()) return@withContext items
        }
        getSampleMovies()
    }

    suspend fun getSeries(): List<PlexMediaItem> = withContext(Dispatchers.IO) {
        val host = configManager.plexHost
        val token = configManager.plexToken
        if (host.isNotBlank()) {
            val items = fetchItemsFromEndpoint(host, token, "/library/sections/2/all")
            if (items.isNotEmpty()) return@withContext items
        }
        getSampleSeries()
    }

    private fun fetchItemsFromEndpoint(host: String, token: String, endpoint: String): List<PlexMediaItem> {
        val cleanHost = host.trimEnd('/')
        val url = if (token.isNotBlank()) {
            val separator = if (endpoint.contains("?")) "&" else "?"
            "$cleanHost$endpoint${separator}X-Plex-Token=$token"
        } else {
            "$cleanHost$endpoint"
        }

        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .header("X-Plex-Client-Identifier", "MLHK-TV-Launcher")
            .header("X-Plex-Product", "MLHK TV+ Launcher")
            .header("X-Plex-Version", "1.0")
            .build()

        return try {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                parsePlexJson(body, cleanHost, token)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parsePlexJson(jsonStr: String, host: String, token: String): List<PlexMediaItem> {
        val result = mutableListOf<PlexMediaItem>()
        try {
            val root = JsonParser.parseString(jsonStr).asJsonObject
            val container = root.getAsJsonObject("MediaContainer") ?: return emptyList()

            val hubs = container.getAsJsonArray("Hub")
            if (hubs != null && hubs.size() > 0) {
                for (hubElement in hubs) {
                    val hubObj = hubElement.asJsonObject
                    val metadata = hubObj.getAsJsonArray("Metadata")
                    if (metadata != null) {
                        for (metaElement in metadata) {
                            parseItem(metaElement.asJsonObject, host, token)?.let { result.add(it) }
                        }
                    }
                }
            }

            val metadata = container.getAsJsonArray("Metadata")
            if (metadata != null) {
                for (metaElement in metadata) {
                    parseItem(metaElement.asJsonObject, host, token)?.let { result.add(it) }
                }
            }
        } catch (e: Exception) {
            // Json parsing error ignored
        }
        return result
    }

    private fun parseItem(obj: JsonObject, host: String, token: String): PlexMediaItem? {
        val ratingKey = obj.get("ratingKey")?.asString ?: return null
        val title = obj.get("title")?.asString ?: return null
        val key = obj.get("key")?.asString ?: "/library/metadata/$ratingKey"
        val type = obj.get("type")?.asString ?: "movie"

        val rawThumb = obj.get("thumb")?.asString
        val rawArt = obj.get("art")?.asString

        val thumbUrl = formatImageUrl(rawThumb, host, token)
        val artUrl = formatImageUrl(rawArt, host, token)

        val year = obj.get("year")?.asInt
        val summary = obj.get("summary")?.asString
        val duration = obj.get("duration")?.asLong
        val viewOffset = obj.get("viewOffset")?.asLong

        val showTitle = obj.get("grandparentTitle")?.asString
        val seasonNum = obj.get("parentIndex")?.asInt
        val episodeNum = obj.get("index")?.asInt
        val rating = obj.get("rating")?.asFloat

        val genres = mutableListOf<String>()
        obj.getAsJsonArray("Genre")?.forEach {
            if (it.isJsonObject) {
                it.asJsonObject.get("tag")?.asString?.let { tag -> genres.add(tag) }
            }
        }

        return PlexMediaItem(
            id = ratingKey,
            ratingKey = ratingKey,
            key = key,
            title = title,
            type = type,
            thumbUrl = thumbUrl,
            artUrl = artUrl,
            year = year,
            summary = summary,
            durationMs = duration,
            viewOffsetMs = viewOffset,
            showTitle = showTitle,
            seasonNumber = seasonNum,
            episodeNumber = episodeNum,
            rating = rating,
            genres = genres
        )
    }

    private fun formatImageUrl(rawPath: String?, host: String, token: String): String? {
        if (rawPath.isNullOrBlank()) return null
        val cleanPath = if (rawPath.startsWith("/")) rawPath else "/$rawPath"
        return if (token.isNotBlank()) {
            val separator = if (cleanPath.contains("?")) "&" else "?"
            "$host$cleanPath${separator}X-Plex-Token=$token"
        } else {
            "$host$cleanPath"
        }
    }

    // High quality curated previews with real image assets for instant visual richness
    private fun getSampleContinueWatching(): List<PlexMediaItem> {
        return listOf(
            PlexMediaItem(
                id = "mock_cw_1",
                ratingKey = "101",
                key = "/library/metadata/101",
                title = "The Massacre at Hawkins Lab",
                type = "episode",
                thumbUrl = "https://image.tmdb.org/t/p/w780/49WJfeN0moxb9IPfGn8AIqMGskD.jpg",
                artUrl = "https://image.tmdb.org/t/p/original/56v2KjBlU4XaOv9rVYEQypROD7P.jpg",
                year = 2024,
                summary = "Terwijl El haar verleden onderzoekt in de ondergrondse faciliteit, doen de vrienden in Hawkins een gruwelijke ontdekking in het Creel House.",
                durationMs = 4500000,
                viewOffsetMs = 2800000,
                showTitle = "Stranger Things",
                seasonNumber = 4,
                episodeNumber = 7,
                rating = 8.8f,
                genres = listOf("Sci-Fi", "Horror", "Drama")
            ),
            PlexMediaItem(
                id = "mock_cw_2",
                ratingKey = "102",
                key = "/library/metadata/102",
                title = "The Batman",
                type = "movie",
                thumbUrl = "https://image.tmdb.org/t/p/w780/74xTEgt7R36Fpooo50r9T25onhq.jpg",
                artUrl = "https://image.tmdb.org/t/p/original/5P8SmMzSNYikXpxil6BYz9G660F.jpg",
                year = 2022,
                summary = "Wanneer een sadistische seriemoordenaar de politieke elite van Gotham teistert, leidt een spoor van raadsels Batman naar de donkere onderwereld.",
                durationMs = 10560000,
                viewOffsetMs = 4500000,
                rating = 7.8f,
                genres = listOf("Misdaad", "Mysterie", "Actie")
            ),
            PlexMediaItem(
                id = "mock_cw_3",
                ratingKey = "103",
                key = "/library/metadata/103",
                title = "Connor's Wedding",
                type = "episode",
                thumbUrl = "https://image.tmdb.org/t/p/w780/e7a2uW00Z8n3vE4y5eL5qH0kK.jpg",
                artUrl = "https://image.tmdb.org/t/p/original/e8n01aG8Z1m1uR8eK5rN8vF1uA.jpg",
                year = 2023,
                summary = "Terwijl Connor en Willa zich voorbereiden op hun bruiloft op een jacht, ontvangt de familie schokkend nieuws over Logan Roy.",
                durationMs = 3600000,
                viewOffsetMs = 1900000,
                showTitle = "Succession",
                seasonNumber = 4,
                episodeNumber = 3,
                rating = 9.8f,
                genres = listOf("Drama")
            )
        )
    }

    private fun getSampleRecentlyAdded(): List<PlexMediaItem> {
        return listOf(
            PlexMediaItem(
                id = "mock_ra_1",
                ratingKey = "201",
                key = "/library/metadata/201",
                title = "Dune: Part Two",
                type = "movie",
                thumbUrl = "https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg",
                artUrl = "https://image.tmdb.org/t/p/original/xOMo8BRK7PfcJv9JCnx7s520DRq.jpg",
                year = 2024,
                summary = "Paul Atreides verenigt zich met Chani en de Vrijmans om wraak te nemen op de samenzweerders die zijn familie hebben vernietigd. Hij staat voor een keuze tussen de liefde van zijn leven en het lot van het universum.",
                durationMs = 9960000,
                rating = 8.6f,
                genres = listOf("Sci-Fi", "Avontuur")
            ),
            PlexMediaItem(
                id = "mock_ra_2",
                ratingKey = "202",
                key = "/library/metadata/202",
                title = "Oppenheimer",
                type = "movie",
                thumbUrl = "https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg",
                artUrl = "https://image.tmdb.org/t/p/original/fm6KqXpk3M2HVveHwCrBSSBaO0V.jpg",
                year = 2023,
                summary = "Het verhaal van de Amerikaanse natuurkundige J. Robert Oppenheimer en zijn rol in de ontwikkeling van de atoombom tijdens het Manhattan Project.",
                durationMs = 10800000,
                rating = 8.9f,
                genres = listOf("Biografie", "Drama", "Geschiedenis")
            ),
            PlexMediaItem(
                id = "mock_ra_3",
                ratingKey = "203",
                key = "/library/metadata/203",
                title = "Fallout",
                type = "show",
                thumbUrl = "https://image.tmdb.org/t/p/w500/AnsZuLg4o5uG8912qG5bQ8g7e.jpg",
                artUrl = "https://image.tmdb.org/t/p/original/mDeUmjC6M9rY7qD00fC3hT6bE.jpg",
                year = 2024,
                summary = "In een post-apocalyptisch Los Angeles na een nucleaire oorlog verlaten bewoners van luxueuze schuilkelders hun beschermde wereld.",
                rating = 8.5f,
                genres = listOf("Sci-Fi", "Actie", "Avontuur")
            ),
            PlexMediaItem(
                id = "mock_ra_4",
                ratingKey = "204",
                key = "/library/metadata/204",
                title = "Interstellar",
                type = "movie",
                thumbUrl = "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
                artUrl = "https://image.tmdb.org/t/p/original/rAiYTsqJJR6vo0m0bYcZlh18v.jpg",
                year = 2014,
                summary = "Wanneer de aarde onbewoonbaar dreigt te worden, reist een team ontdekkingsreizigers door een pas ontdekt wormgat in een poging het voortbestaan van de mensheid te verzekeren.",
                durationMs = 10140000,
                rating = 8.7f,
                genres = listOf("Sci-Fi", "Avontuur", "Drama")
            ),
            PlexMediaItem(
                id = "mock_ra_5",
                ratingKey = "205",
                key = "/library/metadata/205",
                title = "Arcane",
                type = "show",
                thumbUrl = "https://image.tmdb.org/t/p/w500/fqldf2t8ztc9aiwn397mlMacLJ9.jpg",
                artUrl = "https://image.tmdb.org/t/p/original/q8eejuyez1b7v02nI9iQ0e3h.jpg",
                year = 2024,
                summary = "Tussen de rijke utopie Piltover en de vervallen ondergrond van Zaun strijden twee zussen aan weerszijden van een magische en technologische oorlog.",
                rating = 9.0f,
                genres = listOf("Animatie", "Actie", "Sci-Fi")
            )
        )
    }

    private fun getSampleRecommended(): List<PlexMediaItem> {
        return getSampleRecentlyAdded().reversed()
    }

    private fun getSampleMovies(): List<PlexMediaItem> {
        return listOf(
            getSampleRecentlyAdded()[0], // Dune 2
            getSampleRecentlyAdded()[1], // Oppenheimer
            getSampleRecentlyAdded()[3], // Interstellar
            getSampleContinueWatching()[1] // The Batman
        )
    }

    private fun getSampleSeries(): List<PlexMediaItem> {
        return listOf(
            getSampleContinueWatching()[0], // Stranger Things
            getSampleContinueWatching()[2], // Succession
            getSampleRecentlyAdded()[2], // Fallout
            getSampleRecentlyAdded()[4] // Arcane
        )
    }
}
