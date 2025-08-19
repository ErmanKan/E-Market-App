package com.ermanderici.casestudy.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ermanderici.casestudy.data.ProductRepository
import com.ermanderici.casestudy.data.CartRepository
import com.ermanderici.casestudy.model.ProductModel
import com.ermanderici.casestudy.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductUiState(
    val products: List<ProductModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isUserInitiatedRefresh: Boolean = false,
    val searchQuery: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _originalProductsResource = MutableStateFlow<Resource<List<ProductModel>>>(Resource.Loading())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var searchJob: Job? = null
    private var fetchJob: Job? = null

    val productsUiState: StateFlow<ProductUiState> =
        combine(
            _originalProductsResource,
            _searchQuery
        ) { resource, query ->
            val isLoading = resource is Resource.Loading
            val errorMessage = if (resource is Resource.Error) resource.message else null
            val originalData = if (resource is Resource.Success) resource.data ?: emptyList() else emptyList()

            val filteredProducts = if (query.isBlank()) {
                originalData
            } else {
                originalData.filter { product ->
                    product.name.contains(query, ignoreCase = true)
                }
            }
            ProductUiState(
                products = filteredProducts,
                isLoading = isLoading,
                errorMessage = errorMessage,
                searchQuery = query
            )
        }
            .catch { e ->
                emit(ProductUiState(errorMessage = "Flow processing error: ${e.localizedMessage}", isLoading = false))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = ProductUiState(isLoading = true)
            )

    private val _productFavoriteUpdatedEvent = MutableLiveData<ProductModel?>()
    val productFavoriteUpdatedEvent: LiveData<ProductModel?> = _productFavoriteUpdatedEvent

    private val _cartMessage = MutableLiveData<String?>()
    val cartMessage: LiveData<String?> = _cartMessage

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    init {
        fetchProducts()
    }

    fun fetchProducts(isRefresh: Boolean = false) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _originalProductsResource.value = Resource.Loading(if (isRefresh) _originalProductsResource.value.data else null)
            productRepository.getProducts()
                .collect { resource ->
                    _originalProductsResource.value = resource
                }
        }
    }

    fun searchProducts(query: String) {
        searchJob?.cancel()
        _searchQuery.value = query
    }

    fun toggleFavoriteStatus(productToToggle: ProductModel) {
        viewModelScope.launch {
            val newFavoriteState = !productToToggle.isFavorite
            when (val resource = productRepository.updateFavoriteStatus(productToToggle.id, newFavoriteState)) {
                is Resource.Success -> {
                    val updatedProductFromRepo = resource.data
                    if (updatedProductFromRepo != null) {
                        if (_originalProductsResource.value is Resource.Success) {
                            val currentProducts = (_originalProductsResource.value as Resource.Success<List<ProductModel>>).data ?: emptyList()
                            val updatedList = currentProducts.map {
                                if (it.id == updatedProductFromRepo.id) updatedProductFromRepo else it
                            }
                            _originalProductsResource.value = Resource.Success(updatedList)
                        }
                        _productFavoriteUpdatedEvent.postValue(updatedProductFromRepo)
                    } else {
                        _toastMessage.postValue("Favorite updated, but no data returned.")
                        fetchProducts()
                    }
                }
                is Resource.Error -> {
                    _toastMessage.postValue(resource.message ?: "Failed to update favorite")
                }
                is Resource.Loading -> { }
            }
        }
    }

    fun addToCart(product: ProductModel) {
        viewModelScope.launch {
            cartRepository.addProductToCart(product)
            _cartMessage.postValue("${product.name} added to cart!")
        }
    }

    fun onFavoriteEventHandled() {
        _productFavoriteUpdatedEvent.value = null
    }

    fun onCartMessageShown() {
        _cartMessage.value = null
    }

    fun onToastMessageShown() {
        _toastMessage.value = null
    }
}
