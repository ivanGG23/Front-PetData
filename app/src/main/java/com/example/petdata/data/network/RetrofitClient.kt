package com.example.petdata.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    //Bd-desplegada
    private const val BASE_URL = "https://api-gateway-z8qa.onrender.com/"
    //mi cell
    //private const val BASE_URL = "http://172.20.10.6:3000/"
    //mi house
    //private const val BASE_URL = "http://192.168.1.11:3000/"
    //up
    //private const val BASE_URL = "http://10.10.0.54:3000/"

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