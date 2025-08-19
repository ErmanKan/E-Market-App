package com.ermanderici.casestudy.ui.productDetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ermanderici.casestudy.data.ProductRepository
import com.ermanderici.casestudy.model.ProductModel
import com.ermanderici.casestudy.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductDetailUiState(
    val isLoading: Boolean = false,
    val product: ProductModel? = null,
    val errorMessage: String? = null,
    val isFavorite: Boolean = false,
    val actionMessage: String? = null
)

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId: String = savedStateHandle.get<String>("productId")
        ?: throw IllegalStateException("Product ID not found in SavedStateHandle. Ensure it's passed as a navigation argument.")

    private val _uiState = MutableStateFlow(ProductDetailUiState(isLoading = true))
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    init {
        if (productId.isNotBlank()) {
            loadProductDetails()
        } else {
            _uiState.value = ProductDetailUiState(errorMessage = "Invalid Product ID provided.")
        }
    }

    private fun loadProductDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val resource = productRepository.getProductById(productId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            product = resource.data,
                            isFavorite = resource.data?.isFavorite ?: false,
                            errorMessage = if (resource.data == null) "Product not found." else null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            product = null,
                            errorMessage = resource.message ?: "Failed to load product details."
                        )
                    }
                }
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun toggleFavorite() {
        val currentProduct = _uiState.value.product ?: return
        val newFavoriteState = !currentProduct.isFavorite

        viewModelScope.launch {
            when (val resource = productRepository.updateFavoriteStatus(currentProduct.id, newFavoriteState)) {
                is Resource.Success -> {
                    resource.data?.let { updatedProduct ->
                        _uiState.update {
                            it.copy(
                                product = updatedProduct,
                                isFavorite = updatedProduct.isFavorite,
                                actionMessage = if (updatedProduct.isFavorite) {
                                    "${updatedProduct.name} added to favorites."
                                } else {
                                    "${updatedProduct.name} removed from favorites."
                                }
                            )
                        }
                    } ?: _uiState.update {
                        it.copy(errorMessage = "Failed to update UI: Product data missing after update.")
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(errorMessage = resource.message ?: "Failed to update favorite status.")
                    }
                }
                is Resource.Loading -> {  }
            }
        }
    }

    fun addToCart() {
        val currentProduct = _uiState.value.product ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(actionMessage = "${currentProduct.name} added to cart (simulation).")
            }
        }
    }

    fun onActionMessageShown() {
        _uiState.update { it.copy(actionMessage = null) }
    }

    fun onErrorMessageShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
