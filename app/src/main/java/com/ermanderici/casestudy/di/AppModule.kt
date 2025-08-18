package com.ermanderici.casestudy.di

import com.ermanderici.casestudy.network.ApiService
import com.ermanderici.casestudy.network.RetrofitInstance
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApiService(): ApiService {
        return RetrofitInstance.api
    }
}
