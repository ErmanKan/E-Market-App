package com.ermanderici.casestudy.data

import androidx.room.*
import com.ermanderici.casestudy.model.CartItemModel
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Query("SELECT * FROM cart_items")
    fun getAllCartItems(): Flow<List<CartItemModel>>

    @Query("SELECT * FROM cart_items WHERE productId = :productId")
    suspend fun getCartItemByProductId(productId: String): CartItemModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateItem(cartItem: CartItemModel)

    @Query("UPDATE cart_items SET quantity = :newQuantity WHERE productId = :productId")
    suspend fun updateQuantity(productId: String, newQuantity: Int): Int

    @Delete
    suspend fun deleteItem(cartItem: CartItemModel): Int

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun deleteItemByProductId(productId: String): Int

    @Query("DELETE FROM cart_items")
    suspend fun clearCart(): Int
}