package nl.mlhk.tvlauncher.data

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.mlhk.tvlauncher.model.PlexMediaItem
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

data class PlexPinInfo(
    val id: Long,
    val code: String,
    val qrUrl: String
)

class PlexClient(private val configManager: ConfigManager) {

    private val httpClient: OkHttpClient = createUnsafeOkHttpClient()

    suspend fun getContinueWatching(): List<PlexMediaItem> = withContext(Dispatchers.IO) {
        val host = configManager.plexHost
        val token = configManager.plexToken

        if (host.isBlank() || token.isBlank()) return@withContext emptyList()

        val endpoints = listOf("/hubs/continueWatching", "/library/onDeck")
        for (endpoint in endpoints) {
            val items = fetchItemsFromEndpoint(host, token, endpoint)
            if (items.isNotEmpty()) return@withContext items
        }
        emptyList()
    }

    suspend fun getRecentlyAdded(): List<PlexMediaItem> = withContext(Dispatchers.IO) {
        val host = configManager.plexHost
        val token = configManager.plexToken

        if (host.isBlank() || token.isBlank()) return@withContext emptyList()

        val endpoints = listOf("/hubs/recentlyAdded", "/library/recentlyAdded")
        for (endpoint in endpoints) {
            val items = fetchItemsFromEndpoint(host, token, endpoint)
            if (items.isNotEmpty()) return@withContext items
        }
        emptyList()
    }

    suspend fun getRecommended(): List<PlexMediaItem> = withContext(Dispatchers.IO) {
        val host = configManager.plexHost
        val token = configManager.plexToken

        if (host.isBlank() || token.isBlank()) return@withContext emptyList()

        val items = fetchItemsFromEndpoint(host, token, "/hubs/promoted")
        if (items.isNotEmpty()) return@withContext items
        fetchItemsFromEndpoint(host, token, "/library/all")
    }

    suspend fun getMovies(): List<PlexMediaItem> = withContext(Dispatchers.IO) {
        val host = configManager.plexHost
        val token = configManager.plexToken
        if (host.isBlank() || token.isBlank()) return@withContext emptyList()
        fetchItemsFromEndpoint(host, token, "/library/sections/1/all")
    }

    suspend fun getSeries(): List<PlexMediaItem> = withContext(Dispatchers.IO) {
        val host = configManager.plexHost
        val token = configManager.plexToken
        if (host.isBlank() || token.isBlank()) return@withContext emptyList()
        fetchItemsFromEndpoint(host, token, "/library/sections/2/all")
    }

    // Plex.tv PIN Authentication (plex.tv/link)
    suspend fun createPin(): PlexPinInfo? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://plex.tv/api/v2/pins?strong=true")
                .header("Accept", "application/json")
                .header("X-Plex-Product", "MLHK TV+ Launcher")
                .header("X-Plex-Client-Identifier", "mlhk-tv-launcher-diw7022")
                .post("".toRequestBody("application/json".toMediaType()))
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val obj = JsonParser.parseString(body).asJsonObject
                val id = obj.get("id").asLong
                val code = obj.get("code").asString
                val qr = obj.get("qr")?.asString ?: "https://plex.tv/api/v2/pins/qr/$code"
                PlexPinInfo(id, code, qr)
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun pollPin(pinId: Long): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://plex.tv/api/v2/pins/$pinId")
                .header("Accept", "application/json")
                .header("X-Plex-Client-Identifier", "mlhk-tv-launcher-diw7022")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val obj = JsonParser.parseString(body).asJsonObject
                val token = obj.get("authToken")?.takeIf { !it.isJsonNull }?.asString
                if (!token.isNullOrBlank()) token else null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun fetchItemsFromEndpoint(host: String, token: String, endpoint: String): List<PlexMediaItem> {
        val cleanHost = host.trimEnd('/')
        val separator = if (endpoint.contains("?")) "&" else "?"
        val url = "$cleanHost$endpoint${separator}X-Plex-Token=$token"

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
        val separator = if (cleanPath.contains("?")) "&" else "?"
        return "$host$cleanPath${separator}X-Plex-Token=$token"
    }

    private fun createUnsafeOkHttpClient(): OkHttpClient {
        return try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .connectTimeout(4, TimeUnit.SECONDS)
                .readTimeout(6, TimeUnit.SECONDS)
                .build()
        } catch (e: Exception) {
            OkHttpClient()
        }
    }
}
