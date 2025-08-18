package com.ermanderici.casestudy.ui.home

import androidx.lifecycle.*
import com.ermanderici.casestudy.network.ProductRepository
import com.ermanderici.casestudy.model.ProductModel
import com.ermanderici.casestudy.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductUiState(
    val products: List<ProductModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isUserInitiatedRefresh: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    val productsUiState: StateFlow<ProductUiState> =
        productRepository.getProducts()
            .map { resource ->
                when (resource) {
                    is Resource.Success -> ProductUiState(products = resource.data ?: emptyList(), isLoading = false)
                    is Resource.Error -> ProductUiState(
                        errorMessage = resource.message,
                        products = resource.data ?: emptyList(), // Show cached data on error if available
                        isLoading = false
                    )
                    is Resource.Loading -> ProductUiState(
                        isLoading = true,
                        products = resource.data ?: emptyList() // Show cached data while loading
                    )
                }
            }
            .catch { e ->
                emit(ProductUiState(errorMessage = "Flow collection error: ${e.localizedMessage}", isLoading = false))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = ProductUiState(isLoading = true) // Initial state before flow emits
            )

    private val _productFavoriteUpdatedEvent = MutableLiveData<ProductModel?>()
    val productFavoriteUpdatedEvent: LiveData<ProductModel?> = _productFavoriteUpdatedEvent

    private val _cartMessage = MutableLiveData<String?>()
    val cartMessage: LiveData<String?> = _cartMessage

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    fun toggleFavoriteStatus(productToToggle: ProductModel) {
        viewModelScope.launch {
            val newFavoriteState = !productToToggle.isFavorite
            when (val resource = productRepository.updateFavoriteStatus(productToToggle.id, newFavoriteState)) {
                is Resource.Success -> {
                    _productFavoriteUpdatedEvent.postValue(resource.data)
                }
                is Resource.Error -> {
                    // For instance, you could set an error message on a different LiveData/StateFlow
                    _toastMessage.postValue(resource.message ?: "Failed to update favorite")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun addToCart(product: ProductModel) {
        viewModelScope.launch {
            //TODO Cart Logic
            _cartMessage.postValue("${product.name} added to cart!")
        }
    }

    fun onFavoriteEventHandled() {
        _productFavoriteUpdatedEvent.value = null
    }

    fun onCartMessageShown() {
        _cartMessage.value = null
    }
}
