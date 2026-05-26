package com.kabindra.tv.iptv.presentation.di

import com.kabindra.tv.iptv.domain.usecase.remote.livetv.LiveTVUseCase
import com.kabindra.tv.iptv.domain.usecase.remote.movie.MovieBrowseUseCase
import com.kabindra.tv.iptv.domain.usecase.remote.movie.MovieDetailUseCase
import com.kabindra.tv.iptv.domain.usecase.room.LiveTVRoomUseCase
import com.kabindra.tv.iptv.domain.usecase.room.LoginRoomUseCase
import com.kabindra.tv.iptv.domain.usecase.room.MovieRoomUseCase
import com.kabindra.tv.iptv.domain.usecase.xtream.livetv.LiveTVXtreamUseCase
import com.kabindra.tv.iptv.domain.usecase.xtream.movie.MovieBrowseXtreamUseCase
import com.kabindra.tv.iptv.domain.usecase.xtream.movie.MovieDetailXtreamUseCase
import com.kabindra.tv.iptv.presentation.ui.screen.dashboard.DashboardViewModel
import com.kabindra.tv.iptv.presentation.ui.screen.livetv.player.LiveTVPlayerViewModel
import com.kabindra.tv.iptv.presentation.ui.screen.login.LoginViewModel
import com.kabindra.tv.iptv.presentation.ui.screen.movie.content.MovieContentViewModel
import com.kabindra.tv.iptv.presentation.ui.screen.movie.detail.MovieDetailViewModel
import com.kabindra.tv.iptv.presentation.ui.screen.movie.player.MoviePlayerViewModel
import com.kabindra.tv.iptv.presentation.ui.screen.splash.SplashViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationUseCaseModule = module {
    singleOf(::LiveTVUseCase)
    singleOf(::MovieBrowseUseCase)
    singleOf(::MovieDetailUseCase)

    singleOf(::LiveTVXtreamUseCase)
    singleOf(::MovieBrowseXtreamUseCase)
    singleOf(::MovieDetailXtreamUseCase)

    singleOf(::LiveTVRoomUseCase)
    singleOf(::LoginRoomUseCase)
    singleOf(::MovieRoomUseCase)
}

val presentationViewModelModule = module {
    viewModelOf(::SplashViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::DashboardViewModel)
    viewModelOf(::LiveTVPlayerViewModel)
    viewModelOf(::MovieContentViewModel)
    viewModelOf(::MovieDetailViewModel)
    viewModelOf(::MoviePlayerViewModel)
}
