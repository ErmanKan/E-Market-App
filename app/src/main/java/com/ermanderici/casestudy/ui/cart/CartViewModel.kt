package com.ermanderici.casestudy.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ermanderici.casestudy.data.CartRepository
import com.ermanderici.casestudy.model.CartItemActions
import com.ermanderici.casestudy.model.CartItemModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel(), CartItemActions {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val cartItems: StateFlow<List<CartItemModel>> =
        cartRepository.getCartItems()
            .onStart { _isLoading.value = true }
            .onEach { _isLoading.value = false }
            .catch {
                _isLoading.value = false
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList()
            )

    val totalPrice: StateFlow<Double> = cartItems.map { items ->
        items.sumOf { item ->
            try {
                (item.price.replace(",", ".").toDoubleOrNull() ?: 0.0) * item.quantity
            } catch (e: NumberFormatException) {
                0.0
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = 0.0
    )

    override fun onQuantityIncrease(cartItem: CartItemModel) {
        viewModelScope.launch {
            cartRepository.updateItemQuantity(cartItem.productId, cartItem.quantity + 1)
        }
    }

    override fun onQuantityDecrease(cartItem: CartItemModel) {
        viewModelScope.launch {
            val newQuantity = cartItem.quantity - 1
            if (newQuantity > 0) {
                cartRepository.updateItemQuantity(cartItem.productId, newQuantity)
            } else {
                cartRepository.removeProductFromCart(cartItem.productId)
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            cartRepository.clearCart()
        }
    }
}
