package com.kabindra.player.player.util

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.TransferListener
import androidx.media3.exoplayer.DefaultLoadControl.Builder
import androidx.media3.exoplayer.DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
import androidx.media3.exoplayer.DefaultLoadControl.DEFAULT_MAX_BUFFER_MS
import androidx.media3.exoplayer.DefaultLoadControl.DEFAULT_MIN_BUFFER_MS
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.kabindra.player.R

object ExoPlayerUtils {

    @OptIn(UnstableApi::class)
    fun initializeExoPlayer(
        context: Context,
        transferListener: TransferListener? = null
    ): ExoPlayer {
        // Build a DefaultHttpDataSource.Factory with cross-protocol redirects enabled.
        val httpDataSourceFactory: DefaultHttpDataSource.Factory = DefaultHttpDataSource.Factory()
        httpDataSourceFactory.setUserAgent(
            Util.getUserAgent(
                context, context.resources.getString(R.string.APP_NAME)
            )
        )
        httpDataSourceFactory.setConnectTimeoutMs(DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS)
        httpDataSourceFactory.setReadTimeoutMs(DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS)
        httpDataSourceFactory.setAllowCrossProtocolRedirects(true)
        // Wrap the DefaultHttpDataSource.Factory in a DefaultDataSource.Factory, which adds in
        // support for requesting data from other sources (e.g., files, resources, etc).
        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
        if (transferListener != null) {
            dataSourceFactory.setTransferListener(transferListener)
        }

        val trackSelectionFactory = AdaptiveTrackSelection.Factory()
        val trackSelector = DefaultTrackSelector(context, trackSelectionFactory)
        /*trackSelector.setParameters(trackSelector.buildUponParameters().setMaxVideoSizeSd())*/

        val minBufferMs = DEFAULT_MIN_BUFFER_MS
        val maxBufferMs = DEFAULT_MAX_BUFFER_MS
        val bufferForPlaybackMs = /*DEFAULT_BUFFER_FOR_PLAYBACK_MS*/250
        val bufferForPlaybackAfterRebufferMs = DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS

        val loadControl = Builder()
            .setBufferDurationsMs(
                minBufferMs, maxBufferMs, bufferForPlaybackMs, bufferForPlaybackAfterRebufferMs
            )
            .build()

        val renderersFactory: RenderersFactory = buildRenderersFactory(context, true)

        return ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .setRenderersFactory(renderersFactory)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .setSeekBackIncrementMs(10000)
            .setSeekForwardIncrementMs(10000)
            .build()
    }

    private fun useExtensionRenderers(): Boolean {
        return true //"withExtensions" == BuildConfig.FLAVOR
    }

    @OptIn(UnstableApi::class)
    private fun buildRenderersFactory(
        context: Context, preferExtensionRenderer: Boolean
    ): RenderersFactory {
        val extensionRendererMode =
            if (useExtensionRenderers())
                (if (preferExtensionRenderer)
                    DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                else
                    DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
            else
                DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF

        return DefaultRenderersFactory(context.applicationContext)
            .setExtensionRendererMode(extensionRendererMode)
    }

}
