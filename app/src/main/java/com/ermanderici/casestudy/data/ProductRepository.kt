package com.ermanderici.casestudy.data

import com.ermanderici.casestudy.model.ProductModel
import com.ermanderici.casestudy.util.Resource
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProducts(): Flow<Resource<List<ProductModel>>>
    suspend fun updateFavoriteStatus(productId: String, isFavorite: Boolean): Resource<ProductModel>
    suspend fun getProductById(productId: String): Resource<ProductModel?>
    fun getFavoriteProducts(): Flow<Resource<List<ProductModel>>>
}
