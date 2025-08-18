package com.ermanderici.casestudy.network

import com.ermanderici.casestudy.model.ProductModel
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

object RetrofitInstance {
    private const val BASE_URL = "https://5fc9346b2af77700165ae514.mockapi.io/"

    val api: ApiService by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Using Gson
            .build()
            .create(ApiService::class.java)
    }
}

interface ApiService {
    @GET("products")
    suspend fun getProducts(): Response<List<ProductModel>>
}