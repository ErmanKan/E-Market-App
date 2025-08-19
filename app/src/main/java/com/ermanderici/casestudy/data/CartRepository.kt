package com.ermanderici.casestudy.data // Or your repository package

import com.ermanderici.casestudy.model.CartItemModel
import com.ermanderici.casestudy.model.ProductModel
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCartItems(): Flow<List<CartItemModel>>
    suspend fun addProductToCart(product: ProductModel)
    suspend fun removeProductFromCart(productId: String)
    suspend fun updateItemQuantity(productId: String, newQuantity: Int)
    suspend fun clearCart()
}