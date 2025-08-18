package com.ermanderici.casestudy.model

import com.google.gson.annotations.SerializedName

data class ProductModel(
    @SerializedName("id") val id: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("model") val model: String,
    @SerializedName("description") val description: String,
    @SerializedName("price") val price: String,
    @SerializedName("image") val image: String,
    @SerializedName("name") val name: String,
    @SerializedName("createdAt") val createdAt: String,
    var isFavorite: Boolean = false
)

    