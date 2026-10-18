package nl.mlhk.tvlauncher.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import nl.mlhk.tvlauncher.model.PlexMediaItem
import java.io.File

class MediaCacheManager(private val context: Context) {
    private val gson = Gson()
    private val cacheDir = File(context.filesDir, "media_cache").apply { mkdirs() }

    fun saveItems(key: String, items: List<PlexMediaItem>) {
        if (items.isEmpty()) return
        try {
            val file = File(cacheDir, "$key.json")
            val json = gson.toJson(items)
            file.writeText(json)
        } catch (_: Exception) {}
    }

    fun loadItems(key: String): List<PlexMediaItem> {
        return try {
            val file = File(cacheDir, "$key.json")
            if (!file.exists()) return emptyList()
            val json = file.readText()
            if (json.isBlank()) return emptyList()
            val type = object : TypeToken<List<PlexMediaItem>>() {}.type
            gson.fromJson<List<PlexMediaItem>>(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
