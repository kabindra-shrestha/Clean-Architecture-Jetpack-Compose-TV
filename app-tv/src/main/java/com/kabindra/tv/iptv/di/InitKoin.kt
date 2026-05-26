package com.kabindra.tv.iptv.di

import com.kabindra.tv.iptv.data.di.dataApiServiceModule
import com.kabindra.tv.iptv.data.di.dataRepositoryModule
import com.kabindra.tv.iptv.data.di.dataSourceModule
import com.kabindra.tv.iptv.presentation.di.presentationUseCaseModule
import com.kabindra.tv.iptv.presentation.di.presentationViewModelModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            provideAppModule,
            providePlatformModule,
            provideHttpClientModule,
            provideXtreamClientModule,
            dataApiServiceModule,
            dataSourceModule,
            dataRepositoryModule,
            presentationUseCaseModule,
            presentationViewModelModule
        )

    }
}
