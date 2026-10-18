package nl.mlhk.tvlauncher.data

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.mlhk.tvlauncher.model.ServiceStatus
import nl.mlhk.tvlauncher.model.SystemHealthResponse
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class StatusClient(private val configManager: ConfigManager) {

    private val gson = Gson()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .build()

    suspend fun getSystemStatus(): SystemHealthResponse = withContext(Dispatchers.IO) {
        val url = configManager.monitoringApiUrl
        if (url.isBlank()) {
            return@withContext SystemHealthResponse(
                status = "grey",
                message = "Monitoring niet ingesteld",
                services = emptyList()
            )
        }

        val requestBuilder = Request.Builder()
            .url(url)
            .header("Accept", "application/json")

        if (configManager.monitoringApiKey.isNotBlank()) {
            requestBuilder.header("X-API-Key", configManager.monitoringApiKey)
            requestBuilder.header("Authorization", "Bearer ${configManager.monitoringApiKey}")
        }

        try {
            httpClient.newCall(requestBuilder.build()).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext SystemHealthResponse(
                        status = "grey",
                        message = "HTTP ${response.code}",
                        services = emptyList()
                    )
                }
                val body = response.body?.string() ?: return@withContext SystemHealthResponse(
                    status = "grey",
                    message = "Leeg antwoord",
                    services = emptyList()
                )
                gson.fromJson(body, SystemHealthResponse::class.java)
            }
        } catch (e: Exception) {
            SystemHealthResponse(
                status = "grey",
                message = "Niet bereikbaar",
                services = emptyList()
            )
        }
    }
}
