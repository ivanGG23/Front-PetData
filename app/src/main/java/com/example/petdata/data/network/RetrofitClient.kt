package com.example.petdata.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    //Bd-desplegada
    private const val BASE_URL = "https://api-gateway-z8qa.onrender.com/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    // El @Header("Authorization") de cada método en ApiService maneja el token
    fun apiServiceWithToken(token: String): ApiService = apiService
}