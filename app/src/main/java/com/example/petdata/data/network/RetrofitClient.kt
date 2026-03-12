package com.example.petdata.data.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // En desarrollo usa tu IP local, no localhost

    //Bd-desplada
    private const val BASE_URL = "https://api-gateway-z8qa.onrender.com/"
    //mi cell
    //private const val BASE_URL = "http://172.20.10.6:3000/"
    //mi house
    //private const val BASE_URL = "http://192.168.1.11:3000/"
    //up
    //private const val BASE_URL = "http://10.10.0.55:3000/"
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun apiServiceWithToken(token: String): ApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(request)
            })
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}