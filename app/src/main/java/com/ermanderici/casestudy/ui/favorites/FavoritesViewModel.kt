package com.ermanderici.casestudy.ui.favorites // Or your ViewModel package

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ermanderici.casestudy.data.ProductRepository
import com.ermanderici.casestudy.model.ProductModel
import com.ermanderici.casestudy.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesScreenState(
    val isLoading: Boolean = false,
    val products: List<ProductModel> = emptyList(),
    val errorMessage: String? = null,
    val productFavoriteUpdatedEvent: ProductModel? = null
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesScreenState())
    val uiState: StateFlow<FavoritesScreenState> = _uiState.asStateFlow()

    init {
        loadFavoriteProducts()
    }

    private fun loadFavoriteProducts() {
        viewModelScope.launch {
            productRepository.getFavoriteProducts()
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .collectLatest { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    products = resource.data ?: emptyList(),
                                    errorMessage = null
                                )
                            }
                        }
                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = resource.message ?: "An unknown error occurred",
                                    products = resource.data ?: it.products
                                )
                            }
                        }
                        is Resource.Loading -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = true,
                                    products = resource.data ?: it.products
                                )
                            }
                        }
                    }
                }
        }
    }

    fun toggleFavoriteStatus(product: ProductModel) {
        viewModelScope.launch {
            val newFavoriteState = !product.isFavorite
            when (val resource = productRepository.updateFavoriteStatus(product.id, newFavoriteState)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(productFavoriteUpdatedEvent = resource.data?.copy(isFavorite = newFavoriteState)) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = resource.message ?: "Failed to update favorite") }
                }
                is Resource.Loading -> {  }
            }
        }
    }

    fun onFavoriteEventHandled() {
        _uiState.update { it.copy(productFavoriteUpdatedEvent = null) }
    }

    fun onErrorMessageShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

