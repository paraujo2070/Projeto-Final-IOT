package br.com.pedro_araujo.coleta_de_dados.di

import br.com.pedro_araujo.coleta_de_dados.network.HttpPublisher
import br.com.pedro_araujo.coleta_de_dados.network.TelemetryPublisher
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    @Singleton
    abstract fun bindTelemetryPublisher(httpPublisher: HttpPublisher): TelemetryPublisher
}
