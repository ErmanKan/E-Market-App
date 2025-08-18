// In a new file, e.g., ProductRepositoryImpl.kt
package com.ermanderici.casestudy.network

import com.ermanderici.casestudy.model.ProductModel
import com.ermanderici.casestudy.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ProductRepository {

    override suspend fun getProducts(): Resource<List<ProductModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getProducts()
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Response body is null")
                } else {
                    Resource.Error("Error: ${response.code()} ${response.message()}")
                }
            } catch (e: IOException) {
                Resource.Error("Network error: Check connection. ${e.localizedMessage}", exception = e)
            } catch (e: Exception) {
                Resource.Error("An unexpected error occurred: ${e.localizedMessage}", exception = e)
            }
        }
    }

    override suspend fun updateFavoriteStatus(productId: String, isFavorite: Boolean): Resource<ProductModel> {
        TODO("Not yet implemented")

    }

    override suspend fun addToCart(product: ProductModel): Resource<String> {
        TODO("Not yet implemented")
    }
}
