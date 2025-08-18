// In ProductRepositoryImpl.kt
package com.ermanderici.casestudy.network

import com.ermanderici.casestudy.data.ProductDao
import com.ermanderici.casestudy.model.ProductModel
import com.ermanderici.casestudy.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val productDao: ProductDao
) : ProductRepository {

    override fun getProducts(): Flow<Resource<List<ProductModel>>> = flow {
        val initialCache = productDao.getAllProducts().first()
        emit(Resource.Loading(initialCache))

        try {
            val response = apiService.getProducts()
            if (response.isSuccessful) {
                response.body()?.let { networkProducts ->
                    val localProductsMap = productDao.getAllProducts().first().associateBy { it.id }

                    val productsToSave = networkProducts.map { networkProduct ->
                        val localProduct = localProductsMap[networkProduct.id]
                        networkProduct.copy(isFavorite = localProduct?.isFavorite ?: false)
                    }

                    productDao.insertAll(productsToSave)
                } ?: run {
                }
            } else {
                emit(Resource.Error("Network Error: ${response.code()} ${response.message()}", productDao.getAllProducts().first()))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network Connection Error: ${e.localizedMessage}", productDao.getAllProducts().first()))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected Error Fetching Products: ${e.localizedMessage}", productDao.getAllProducts().first()))
        }
        productDao.getAllProducts().map { dbProducts ->
            if (dbProducts.isNotEmpty()) {
                Resource.Success(dbProducts)
            } else {
                Resource.Success(emptyList())
            }
        }.distinctUntilChanged().collect { emit(it) }

    }.flowOn(Dispatchers.IO)

    override suspend fun updateFavoriteStatus(productId: String, isFavorite: Boolean): Resource<ProductModel> {
        return withContext(Dispatchers.IO) {
            try {
                productDao.updateFavoriteStatus(productId, isFavorite)
                val updatedProduct = productDao.getProductById(productId)
                if (updatedProduct != null) {
                    Resource.Success(updatedProduct)
                } else {
                    Resource.Error("Product not found after update.")
                }
            } catch (e: Exception) {
                Resource.Error("Failed to update favorite status in DB: ${e.localizedMessage}", exception = e)
            }
        }
    }
}
