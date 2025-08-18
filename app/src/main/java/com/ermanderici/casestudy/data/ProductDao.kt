package com.ermanderici.casestudy.data

import androidx.room.*
import com.ermanderici.casestudy.model.ProductModel
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    // --- Query Operations (Read) ---

    /**
     * Get all products from the 'products' table.
     * Returns a Flow, so observers are notified of changes.
     */
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductModel>>

    /**
     * Get a single product by its ID.
     * This is a suspend function for a one-time fetch.
     * Returns ProductModel? (nullable) because a product with the given ID might not exist.
     */
    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: String): ProductModel? // Changed to nullable

    /**
     * Get only favorite products (isFavorite = true).
     * Returns a Flow for reactive updates.
     */
    @Query("SELECT * FROM products WHERE isFavorite = 1") // In SQLite, boolean true is often represented as 1
    fun getFavoriteProducts(): Flow<List<ProductModel>>

    // --- Insert Operations (Write) ---

    /**
     * Insert a list of products.
     * If a product already exists (based on primary key), it will be replaced.
     * Suspend function, returns nothing (Unit) by default for inserts.
     * If you need the list of inserted row IDs, change return to List<Long>.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductModel>) // Changed return type to Unit (void)

    /**
     * Insert a single product.
     * If the product already exists, it will be replaced.
     * Suspend function, returns nothing (Unit) by default.
     * If you need the inserted row ID, change return to Long.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductModel) // Changed return type to Unit (void)

    // --- Update Operations (Write) ---

    /**
     * Update an existing product.
     * The product is identified by its primary key.
     * Suspend function. Returns the number of rows updated (Int).
     * If you don't need the count, change return to Unit (void).
     */
    @Update
    suspend fun updateProduct(product: ProductModel): Int

    /**
     * Update the favorite status of a specific product by its ID.
     * Suspend function. Returns the number of rows updated (Int).
     * @Query methods that modify data (UPDATE, DELETE, INSERT) should return Int (rows affected) or Unit.
     */
    @Query("UPDATE products SET isFavorite = :isFavorite WHERE id = :productId")
    suspend fun updateFavoriteStatus(productId: String, isFavorite: Boolean): Int

    // --- Delete Operations (Write) ---

    /**
     * Delete all products from the 'products' table.
     * Suspend function. Returns the number of rows deleted (Int).
     */
    @Query("DELETE FROM products")
    suspend fun deleteAllProducts(): Int
}
