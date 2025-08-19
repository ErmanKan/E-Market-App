package com.ermanderici.casestudy.model

interface CartItemActions {
    fun onQuantityIncrease(cartItem: CartItemModel)
    fun onQuantityDecrease(cartItem: CartItemModel)
}

