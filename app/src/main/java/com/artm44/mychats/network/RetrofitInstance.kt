package com.artm44.mychats.network

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import okhttp3.OkHttpClient
import com.squareup.moshi.Moshi

object RetrofitInstance {
    private const val BASE_URL = "https://faerytea.name:8008/"

    private val moshi = Moshi.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(ScalarsConverterFactory.create()) // Для строковых данных
        .addConverterFactory(MoshiConverterFactory.create(moshi)) // Для JSON
        .client(OkHttpClient())
        .build()

    val api: ApiService = retrofit.create(ApiService::class.java)
}
