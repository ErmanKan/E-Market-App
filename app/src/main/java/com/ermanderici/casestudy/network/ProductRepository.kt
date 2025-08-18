// In ProductRepository.kt
package com.ermanderici.casestudy.network

import com.ermanderici.casestudy.model.ProductModel
import com.ermanderici.casestudy.util.Resource
import kotlinx.coroutines.flow.Flow

interface ProductRepository {

    fun getProducts(): Flow<Resource<List<ProductModel>>> // Changed from suspend fun

    suspend fun updateFavoriteStatus(productId: String, isFavorite: Boolean): Resource<ProductModel>
}
