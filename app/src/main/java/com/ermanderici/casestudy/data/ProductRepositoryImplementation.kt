// In ProductRepositoryImpl.kt
package com.ermanderici.casestudy.data

import com.ermanderici.casestudy.model.ProductModel
import com.ermanderici.casestudy.network.ApiService
import com.ermanderici.casestudy.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch // Added for getFavoriteProducts
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
                    //assume it will always return positive
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

    override suspend fun getProductById(productId: String): Resource<ProductModel?> {
        return withContext(Dispatchers.IO) {
            try {
                val product = productDao.getProductById(productId)
                Resource.Success(product)
            } catch (e: Exception) {
                Resource.Error("Failed to get product by ID from DB: ${e.localizedMessage}", data = null, exception = e)
            }
        }
    }

    // Added getFavoriteProducts method
    override fun getFavoriteProducts(): Flow<Resource<List<ProductModel>>> {
        return productDao.getFavoriteProducts()
            .map<List<ProductModel>, Resource<List<ProductModel>>> { favorites ->
                Resource.Success(favorites)
            }
            .catch { e ->
                emit(Resource.Error("Failed to load favorites from database: ${e.localizedMessage}", emptyList()))
            }
            .flowOn(Dispatchers.IO)
    }
}
