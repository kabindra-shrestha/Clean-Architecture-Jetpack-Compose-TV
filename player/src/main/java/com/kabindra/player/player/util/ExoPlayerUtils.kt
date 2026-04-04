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
import com.kabindra.player.PlayerBufferConfig
import com.kabindra.player.PlayerPerformanceConfig
import com.kabindra.player.R

object ExoPlayerUtils {

    @OptIn(UnstableApi::class)
    fun initializeExoPlayer(
        context: Context,
        transferListener: TransferListener? = null
    ): ExoPlayer {
        return createExoPlayer(
            context = context,
            bufferConfig = PlayerBufferConfig(),
            performanceConfig = PlayerPerformanceConfig(),
            transferListener = transferListener
        )
    }

    @OptIn(UnstableApi::class)
    fun createExoPlayer(
        context: Context,
        bufferConfig: PlayerBufferConfig,
        performanceConfig: PlayerPerformanceConfig,
        transferListener: TransferListener? = null,
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

        val loadControl = Builder()
            .setBufferDurationsMs(
                bufferConfig.minBufferMs,
                bufferConfig.maxBufferMs,
                bufferConfig.bufferForPlaybackMs,
                bufferConfig.bufferForPlaybackAfterRebufferMs
            )
            .setBackBuffer(bufferConfig.backBufferMs, true)
            .build()

        val renderersFactory: RenderersFactory = buildRenderersFactory(
            context,
            performanceConfig.preferExtensionRenderers
        )

        return ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .setRenderersFactory(renderersFactory)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .setSeekBackIncrementMs(performanceConfig.seekBackMs)
            .setSeekForwardIncrementMs(performanceConfig.seekForwardMs)
            .setHandleAudioBecomingNoisy(performanceConfig.handleAudioBecomingNoisy)
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
