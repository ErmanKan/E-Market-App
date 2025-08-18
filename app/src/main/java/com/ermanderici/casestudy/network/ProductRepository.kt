package com.ermanderici.casestudy.network // Example package

import com.ermanderici.casestudy.model.ProductModel
import com.ermanderici.casestudy.util.Resource // For handling loading/success/error states

interface ProductRepository {
    // Fetches all products
    suspend fun getProducts(): Resource<List<ProductModel>>

    // Updates the favorite status of a product
    // This might interact with a local database or a remote API
    suspend fun updateFavoriteStatus(productId: String, isFavorite: Boolean): Resource<ProductModel>

    // Adds product to cart
    suspend fun addToCart(product: ProductModel): Resource<String> // Returns a success/error message or status
}
