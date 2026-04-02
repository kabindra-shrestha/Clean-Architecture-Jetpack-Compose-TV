package com.kabindra.tv.iptv.di

import androidx.compose.material3.SnackbarHostState
import com.kabindra.tv.iptv.data.repository.remote.livetv.LiveTVRepositoryImpl
import com.kabindra.tv.iptv.data.repository.remote.movie.MovieRepositoryImpl
import com.kabindra.tv.iptv.data.source.remote.ApiService
import com.kabindra.tv.iptv.data.source.remote.livetv.FakeLiveTVRemoteDataSource
import com.kabindra.tv.iptv.data.source.remote.livetv.LiveTVRemoteDataSource
import com.kabindra.tv.iptv.data.source.remote.movie.FakeMovieRemoteDataSource
import com.kabindra.tv.iptv.data.source.remote.movie.MovieRemoteDataSource
import com.kabindra.tv.iptv.domain.repository.remote.livetv.LiveTVRepository
import com.kabindra.tv.iptv.domain.repository.remote.movie.MovieRepository
import com.kabindra.tv.iptv.domain.usecase.remote.livetv.LiveTVUseCase
import com.kabindra.tv.iptv.domain.usecase.remote.movie.MovieBrowseUseCase
import com.kabindra.tv.iptv.domain.usecase.remote.movie.MovieDetailUseCase
import com.kabindra.tv.iptv.presentation.ui.screen.livetv.player.LiveTVPlayerViewModel
import com.kabindra.tv.iptv.presentation.ui.screen.movie.content.MovieContentViewModel
import com.kabindra.tv.iptv.presentation.ui.screen.movie.detail.MovieDetailViewModel
import com.kabindra.tv.iptv.presentation.ui.screen.movie.player.MoviePlayerViewModel
import com.kabindra.tv.iptv.presentation.ui.screen.splash.SplashViewModel
import com.kabindra.tv.iptv.utils.constants.Header.Companion.HEADER_USER_DEVICE
import com.kabindra.tv.iptv.utils.constants.Header.Companion.HEADER_USER_DEVICE_APP_VERSION
import com.kabindra.tv.iptv.utils.constants.Header.Companion.HEADER_USER_DEVICE_APP_VERSION_CODE
import com.kabindra.tv.iptv.utils.constants.Header.Companion.HEADER_USER_DEVICE_BRAND
import com.kabindra.tv.iptv.utils.constants.Header.Companion.HEADER_USER_DEVICE_BUILD
import com.kabindra.tv.iptv.utils.constants.Header.Companion.HEADER_USER_DEVICE_KEY
import com.kabindra.tv.iptv.utils.constants.Header.Companion.HEADER_USER_DEVICE_MODEL
import com.kabindra.tv.iptv.utils.constants.Header.Companion.HEADER_USER_DEVICE_PLATFORM
import com.kabindra.tv.iptv.utils.constants.Header.Companion.HEADER_USER_DEVICE_VERSION
import com.kabindra.tv.iptv.utils.getPlatform
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val provideAppModule = module {
    single { SnackbarHostState() }
}

val provideHttpClientModule = module {
    fun provideHttpClient(): HttpClient {
        return HttpClient {
            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.ALL
            }
            install(ContentNegotiation) {
                json(
                    json = Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                        explicitNulls = false
                    },
                    contentType = ContentType.Application.Json
                )
            }
            install(HttpTimeout) {
                socketTimeoutMillis = 60_000
                requestTimeoutMillis = 60_000
            }
            install(UserAgent) {
                agent = getPlatform().userAgent
            }
            install(DefaultRequest) {
                runBlocking {
                    url("baseUrl")

                    contentType(ContentType.Application.Json)

                    val platform = getPlatform()

                    header(HEADER_USER_DEVICE, platform.userDevice + HEADER_USER_DEVICE_KEY)
                    header(HEADER_USER_DEVICE_PLATFORM, platform.devicePlatform)
                    header(HEADER_USER_DEVICE_VERSION, platform.deviceVersion)
                    header(HEADER_USER_DEVICE_BUILD, platform.deviceBuild)
                    header(HEADER_USER_DEVICE_BRAND, platform.deviceBrand)
                    header(HEADER_USER_DEVICE_MODEL, platform.deviceModel)
                    header(HEADER_USER_DEVICE_APP_VERSION, platform.appVersion)
                    header(HEADER_USER_DEVICE_APP_VERSION_CODE, platform.appVersionCode)
                }
            }
        }
    }

    singleOf(::provideHttpClient)
}

val provideApiServiceModule = module {
    singleOf(::ApiService)
}

val provideDataSourceModule = module {
    single<LiveTVRemoteDataSource> { FakeLiveTVRemoteDataSource() }
    single<MovieRemoteDataSource> { FakeMovieRemoteDataSource() }
}

val provideRepositoryModule = module {
    singleOf(::LiveTVRepositoryImpl).bind<LiveTVRepository>()
    singleOf(::MovieRepositoryImpl).bind<MovieRepository>()
}

val provideUseCaseModule = module {
    singleOf(::LiveTVUseCase)
    singleOf(::MovieBrowseUseCase)
    singleOf(::MovieDetailUseCase)
}

val provideViewModelModule = module {
    viewModelOf(::SplashViewModel)
    viewModelOf(::LiveTVPlayerViewModel)
    viewModelOf(::MovieContentViewModel)
    viewModelOf(::MovieDetailViewModel)
    viewModelOf(::MoviePlayerViewModel)
}
