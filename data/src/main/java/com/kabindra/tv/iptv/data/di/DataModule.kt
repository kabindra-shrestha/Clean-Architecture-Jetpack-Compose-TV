package com.kabindra.tv.iptv.data.di

import com.kabindra.tv.iptv.data.repository.remote.livetv.LiveTVRepositoryImpl
import com.kabindra.tv.iptv.data.repository.remote.movie.MovieRepositoryImpl
import com.kabindra.tv.iptv.data.repository.room.LiveTVRoomRepositoryImpl
import com.kabindra.tv.iptv.data.repository.room.LoginRoomRepositoryImpl
import com.kabindra.tv.iptv.data.repository.room.MovieRoomRepositoryImpl
import com.kabindra.tv.iptv.data.repository.xtream.livetv.LiveTVXtreamRepositoryImpl
import com.kabindra.tv.iptv.data.repository.xtream.movie.MovieXtreamRepositoryImpl
import com.kabindra.tv.iptv.data.source.UserCredentialsProviderImpl
import com.kabindra.tv.iptv.data.source.remote.ApiService
import com.kabindra.tv.iptv.data.source.remote.livetv.FakeLiveTVRemoteDataSource
import com.kabindra.tv.iptv.data.source.remote.livetv.LiveTVRemoteDataSource
import com.kabindra.tv.iptv.data.source.remote.movie.FakeMovieRemoteDataSource
import com.kabindra.tv.iptv.data.source.remote.movie.MovieRemoteDataSource
import com.kabindra.tv.iptv.data.source.room.AppDatabase
import com.kabindra.tv.iptv.data.source.room.getDatabaseBuilder
import com.kabindra.tv.iptv.data.source.xtream.XtreamService
import com.kabindra.tv.iptv.data.source.xtream.livetv.LiveTVXtreamDataSource
import com.kabindra.tv.iptv.data.source.xtream.livetv.LiveTVXtreamDataSourceImpl
import com.kabindra.tv.iptv.data.source.xtream.movie.MovieXtreamDataSource
import com.kabindra.tv.iptv.data.source.xtream.movie.MovieXtreamDataSourceImpl
import com.kabindra.tv.iptv.domain.repository.remote.livetv.LiveTVRepository
import com.kabindra.tv.iptv.domain.repository.remote.movie.MovieRepository
import com.kabindra.tv.iptv.domain.repository.room.LiveTVRoomRepository
import com.kabindra.tv.iptv.domain.repository.room.LoginRoomRepository
import com.kabindra.tv.iptv.domain.repository.room.MovieRoomRepository
import com.kabindra.tv.iptv.domain.repository.session.CurrentUserRepository
import com.kabindra.tv.iptv.domain.repository.xtream.livetv.LiveTVXtreamRepository
import com.kabindra.tv.iptv.domain.repository.xtream.movie.MovieXtreamRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataApiServiceModule = module {
    singleOf(::ApiService)
    singleOf(::XtreamService)
}

val dataSourceModule = module {
    single<LiveTVRemoteDataSource> { FakeLiveTVRemoteDataSource() }
    single<MovieRemoteDataSource> { FakeMovieRemoteDataSource() }

    singleOf(::LiveTVXtreamDataSourceImpl).bind<LiveTVXtreamDataSource>()
    singleOf(::MovieXtreamDataSourceImpl).bind<MovieXtreamDataSource>()

    single<AppDatabase> { getDatabaseBuilder(androidContext()) }
    single<CurrentUserRepository> { UserCredentialsProviderImpl(get()) }
}

val dataRepositoryModule = module {
    singleOf(::LiveTVRepositoryImpl).bind<LiveTVRepository>()
    singleOf(::MovieRepositoryImpl).bind<MovieRepository>()

    singleOf(::LiveTVXtreamRepositoryImpl).bind<LiveTVXtreamRepository>()
    singleOf(::MovieXtreamRepositoryImpl).bind<MovieXtreamRepository>()

    singleOf(::LiveTVRoomRepositoryImpl).bind<LiveTVRoomRepository>()
    singleOf(::LoginRoomRepositoryImpl).bind<LoginRoomRepository>()
    singleOf(::MovieRoomRepositoryImpl).bind<MovieRoomRepository>()
}
