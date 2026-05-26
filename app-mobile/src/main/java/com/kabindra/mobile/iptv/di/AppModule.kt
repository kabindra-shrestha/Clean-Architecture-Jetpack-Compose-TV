package com.kabindra.mobile.iptv.di

import androidx.compose.material3.SnackbarHostState
import com.kabindra.mobile.iptv.socket.SocketNotificationStreamRepository
import com.kabindra.mobile.iptv.utils.constants.Header.Companion.HEADER_USER_DEVICE
import com.kabindra.mobile.iptv.utils.constants.Header.Companion.HEADER_USER_DEVICE_APP_VERSION
import com.kabindra.mobile.iptv.utils.constants.Header.Companion.HEADER_USER_DEVICE_APP_VERSION_CODE
import com.kabindra.mobile.iptv.utils.constants.Header.Companion.HEADER_USER_DEVICE_BRAND
import com.kabindra.mobile.iptv.utils.constants.Header.Companion.HEADER_USER_DEVICE_BUILD
import com.kabindra.mobile.iptv.utils.constants.Header.Companion.HEADER_USER_DEVICE_KEY
import com.kabindra.mobile.iptv.utils.constants.Header.Companion.HEADER_USER_DEVICE_MODEL
import com.kabindra.mobile.iptv.utils.constants.Header.Companion.HEADER_USER_DEVICE_PLATFORM
import com.kabindra.mobile.iptv.utils.constants.Header.Companion.HEADER_USER_DEVICE_VERSION
import com.kabindra.mobile.iptv.utils.getPlatform
import com.kabindra.tv.iptv.domain.repository.notification.NotificationStreamRepository
import io.github.saifullah.xtream.Xtream
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
import org.koin.dsl.module

val provideAppModule = module {
    single { SnackbarHostState() }
}

val providePlatformModule = module {
    single<NotificationStreamRepository> { SocketNotificationStreamRepository() }
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

val provideXtreamClientModule = module {
    fun provideXtreamClient(): Xtream {
        return Xtream {
            auth {
                protocol = "http"
                host = "tv.quierover.xyz"
                port = 8080
                username = "SAMIR18"
                password = "Banana18"
            }

            socketTimeoutMillis = 30000
            connectTimeoutMillis = 30000
            requestTimeoutMillis = 30000
            useCache = true
            maxRetries = 3
            retryDelayMillis = 1000
        }
    }

    singleOf(::provideXtreamClient)
}
