package com.ermanderici.casestudy.di

import com.ermanderici.casestudy.data.CartDao
import com.ermanderici.casestudy.data.ProductDao
import com.ermanderici.casestudy.data.CartRepository
import com.ermanderici.casestudy.data.CartRepositoryImpl
import com.ermanderici.casestudy.network.ApiService
import com.ermanderici.casestudy.data.ProductRepository
import com.ermanderici.casestudy.data.ProductRepositoryImpl
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
        productDao: ProductDao
    ): ProductRepository {
        return ProductRepositoryImpl(apiService, productDao)
    }

    @Provides
    @Singleton
    fun provideCartRepository(cartDao: CartDao): CartRepository {
        return CartRepositoryImpl(cartDao)
    }
}