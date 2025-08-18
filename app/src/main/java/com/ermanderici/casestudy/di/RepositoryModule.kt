package com.ermanderici.casestudy.di

import com.ermanderici.casestudy.data.ProductDao // <--- Add this import
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
    @Singleton
    fun provideProductRepository(
        apiService: ApiService,
        productDao: ProductDao // <--- Add productDao as a parameter here
    ): ProductRepository {
        // Now Hilt can inject both apiService and productDao
        return ProductRepositoryImpl(apiService, productDao)
    }
}