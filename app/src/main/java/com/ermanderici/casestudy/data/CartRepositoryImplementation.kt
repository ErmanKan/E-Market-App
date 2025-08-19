package com.ermanderici.casestudy.data

import com.ermanderici.casestudy.model.CartItemModel
import com.ermanderici.casestudy.model.ProductModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val cartDao: CartDao
) : CartRepository {

    override fun getCartItems(): Flow<List<CartItemModel>> {
        return cartDao.getAllCartItems()
    }

    override suspend fun addProductToCart(product: ProductModel) {
        val existingItem = cartDao.getCartItemByProductId(product.id)
        if (existingItem != null) {
            val updatedItem = existingItem.copy(quantity = existingItem.quantity + 1)
            cartDao.insertOrUpdateItem(updatedItem)
        } else {
            val newItem = CartItemModel(
                productId = product.id,
                name = product.name,
                price = product.price,
                image = product.image,
                quantity = 1
            )
            cartDao.insertOrUpdateItem(newItem)
        }
    }

    override suspend fun removeProductFromCart(productId: String) {
        cartDao.deleteItemByProductId(productId)
    }

    override suspend fun updateItemQuantity(productId: String, newQuantity: Int) {
        if (newQuantity > 0) {
            cartDao.updateQuantity(productId, newQuantity)
        } else {
            cartDao.deleteItemByProductId(productId)
        }
    }

    override suspend fun clearCart() {
        cartDao.clearCart()
    }
}
