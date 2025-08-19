package com.ermanderici.casestudy.data

import androidx.room.*
import com.ermanderici.casestudy.model.ProductModel
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {


    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductModel>>


    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: String): ProductModel?

    @Query("SELECT * FROM products WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteProducts(): Flow<List<ProductModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductModel>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductModel)

    @Update
    suspend fun updateProduct(product: ProductModel): Int

    @Query("UPDATE products SET isFavorite = :isFavorite WHERE id = :productId")
    suspend fun updateFavoriteStatus(productId: String, isFavorite: Boolean): Int

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts(): Int
}
