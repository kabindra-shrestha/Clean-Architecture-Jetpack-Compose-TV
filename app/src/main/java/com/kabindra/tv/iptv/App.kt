package com.kabindra.tv.iptv

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.kabindra.tv.iptv.presentation.ui.screen.navigation.MainScreen
import com.kabindra.tv.iptv.presentation.ui.theme.JetpackComposeTVCleanArchitectureTheme
import okio.FileSystem

@Composable
fun App(modifier: Modifier) {
    JetpackComposeTVCleanArchitectureTheme {
        setSingletonImageLoaderFactory { context ->
            getAsyncImageLoader(context)
        }

        MainScreen()
    }
}

fun getAsyncImageLoader(context: PlatformContext) =
    ImageLoader.Builder(context)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(context, 0.3)
                .strongReferencesEnabled(true)
                .build()
        }
        .diskCachePolicy(CachePolicy.ENABLED)
        .networkCachePolicy(CachePolicy.ENABLED)
        .diskCache {
            newDiskCache()
        }
        .crossfade(true)
        .logger(DebugLogger())
        .build()

fun newDiskCache(): DiskCache {
    return DiskCache.Builder().directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "image_cache")
        .maxSizeBytes(1024L * 1024 * 1024) // 512MB
        .build()
}
