package nl.mlhk.tvlauncher

import android.app.Application
import android.graphics.Bitmap
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache

class MlhkApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .bitmapConfig(Bitmap.Config.RGB_565) // 50% minder geheugen per afbeelding op Android TV
            .allowHardware(true)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.20)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("coil_cache"))
                    .maxSizeBytes(64L * 1024 * 1024) // 64 MB
                    .build()
            }
            .crossfade(150)
            .build()
    }
}
