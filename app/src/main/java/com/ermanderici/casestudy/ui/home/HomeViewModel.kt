package com.ermanderici.casestudy.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ermanderici.casestudy.model.ProductModel
import com.ermanderici.casestudy.network.ProductRepository
import com.ermanderici.casestudy.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _products = MutableLiveData<List<ProductModel>>()
    val products: LiveData<List<ProductModel>> = _products

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _productUpdatedEvent = MutableLiveData<ProductModel?>()
    val productUpdatedEvent: LiveData<ProductModel?> = _productUpdatedEvent

    private val _cartMessage = MutableLiveData<String?>()
    val cartMessage: LiveData<String?> = _cartMessage

    init {
        fetchProducts()
    }

    fun fetchProducts() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            _errorMessage.postValue(null) // Clear previous error
            when (val resource = productRepository.getProducts()) {
                is Resource.Success -> {
                    _products.postValue(resource.data ?: emptyList())
                }
                is Resource.Error -> {
                    _errorMessage.postValue(resource.message ?: "An unknown error occurred")
                }
                is Resource.Loading -> {

                }
            }
            _isLoading.postValue(false)
        }
    }

    fun toggleFavoriteStatus(productToToggle: ProductModel) {
        viewModelScope.launch {
            val newFavoriteState = !productToToggle.isFavorite

            // Optimistically update UI first or update after repository call.
            // For this example, let's update the local list and then call repository.
            // If repository fails, we might need to revert.

            // 1. Update the local list optimistically (or find the product and update)
            val currentList = _products.value?.toMutableList() ?: return@launch
            val productIndex = currentList.indexOfFirst { it.id == productToToggle.id }

            if (productIndex != -1) {
                // Create a new instance for LiveData to recognize the change
                val updatedProduct = currentList[productIndex].copy(isFavorite = newFavoriteState)
                currentList[productIndex] = updatedProduct

                // Call the repository to persist the change
                when (val resource = productRepository.updateFavoriteStatus(updatedProduct.id, newFavoriteState)) {
                    is Resource.Success -> {
                        // Successfully persisted.
                        // Now update the main products list if the repository doesn't directly feed it
                        // and trigger the single update event for the adapter.
                        _products.postValue(currentList) // Reflect the optimistic update
                        _productUpdatedEvent.postValue(updatedProduct) // Notify for specific adapter update
                    }
                    is Resource.Error -> {
                        // Failed to persist. Optionally revert the optimistic update.
                        // For simplicity, just show an error.
                        _errorMessage.postValue(resource.message ?: "Failed to update favorite")
                        // Revert optimistic update (optional):
                        // currentList[productIndex] = productToToggle.copy(isFavorite = productToToggle.isFavorite)
                        // _products.postValue(currentList)
                        // _productUpdatedEvent.postValue(productToToggle)
                    }
                    is Resource.Loading -> { /* Optionally handle loading for this specific action */ }
                }
            }
        }
    }

    fun addToCart(product: ProductModel) {
        viewModelScope.launch {
            // Logic for adding to cart
            // This might also involve a repository call if cart state is persisted
            // e.g., when (val resource = cartRepository.addItem(product)) { ... }
            _cartMessage.postValue("${product.name} added to cart!")
        }
    }

    fun onProductUpdatedEventHandled() {
        _productUpdatedEvent.value = null
    }

    fun onErrorMessageShown() {
        _errorMessage.value = null
    }

    fun onCartMessageShown() {
        _cartMessage.value = null
    }
}
