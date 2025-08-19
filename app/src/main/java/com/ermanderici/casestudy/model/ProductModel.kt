package com.ermanderici.casestudy.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductModel(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val brand: String,
    val model: String,
    val price: String,
    val image: String,
    var isFavorite: Boolean = false
)
