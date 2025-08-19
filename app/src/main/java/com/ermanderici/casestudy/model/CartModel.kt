package com.ermanderici.casestudy.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemModel(
    @PrimaryKey val productId: String,
    val name: String,
    val price: String,
    val image: String,
    var quantity: Int = 1
)
