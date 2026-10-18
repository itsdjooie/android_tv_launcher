package nl.mlhk.tvlauncher.data

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.mlhk.tvlauncher.model.PlexMediaItem
import nl.mlhk.tvlauncher.model.PlexSectionHub
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class PlexClient(private val configManager: ConfigManager) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .build()

    suspend fun getContinueWatching(): List<PlexMediaItem> = withContext(Dispatchers.IO) {
        val host = configManager.plexHost
        val token = configManager.plexToken

        if (host.isBlank()) return@withContext emptyList()

        val endpoints = listOf(
            "/hubs/continueWatching",
            "/library/onDeck"
        )

        for (endpoint in endpoints) {
            val items = fetchItemsFromEndpoint(host, token, endpoint)
            if (items.isNotEmpty()) return@withContext items
        }
        emptyList()
    }

    suspend fun getRecentlyAdded(): List<PlexMediaItem> = withContext(Dispatchers.IO) {
        val host = configManager.plexHost
        val token = configManager.plexToken

        if (host.isBlank()) return@withContext emptyList()

        val endpoints = listOf(
            "/hubs/recentlyAdded",
            "/library/recentlyAdded"
        )

        for (endpoint in endpoints) {
            val items = fetchItemsFromEndpoint(host, token, endpoint)
            if (items.isNotEmpty()) return@withContext items
        }
        emptyList()
    }

    suspend fun getRecommended(): List<PlexMediaItem> = withContext(Dispatchers.IO) {
        val host = configManager.plexHost
        val token = configManager.plexToken

        if (host.isBlank()) return@withContext emptyList()

        val items = fetchItemsFromEndpoint(host, token, "/hubs/promoted")
        if (items.isNotEmpty()) return@withContext items

        // Fallback to library all movies if promoted hub is empty
        fetchItemsFromEndpoint(host, token, "/library/all")
    }

    suspend fun getMovies(): List<PlexMediaItem> = withContext(Dispatchers.IO) {
        val host = configManager.plexHost
        val token = configManager.plexToken
        if (host.isBlank()) return@withContext emptyList()
        fetchItemsFromEndpoint(host, token, "/library/sections/1/all")
    }

    suspend fun getSeries(): List<PlexMediaItem> = withContext(Dispatchers.IO) {
        val host = configManager.plexHost
        val token = configManager.plexToken
        if (host.isBlank()) return@withContext emptyList()
        fetchItemsFromEndpoint(host, token, "/library/sections/2/all")
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

            // Check if items are inside Hubs
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

            // Check direct Metadata array
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
}
