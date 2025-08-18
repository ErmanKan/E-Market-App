package com.ermanderici.casestudy.di

import com.ermanderici.casestudy.network.ApiService
import com.ermanderici.casestudy.network.ProductRepository
import com.ermanderici.casestudy.network.ProductRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton // Or @ActivityRetainedScoped for ViewModel lifecycle
    fun provideProductRepository(apiService: ApiService): ProductRepository {
        return ProductRepositoryImpl(apiService)
    }
}
