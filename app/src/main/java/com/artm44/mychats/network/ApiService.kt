package com.artm44.mychats.network

import com.artm44.mychats.models.LoginResponse
import com.artm44.mychats.models.Message
import com.artm44.mychats.models.MessageResponse
import com.artm44.mychats.models.RegisterResponse
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("/addusr")
    suspend fun registerUser(@Field("name") username: String): Response<String> // Ответ — plain text

    // Логин
    @POST("/login")
    suspend fun loginUser(@Body requestBody: Map<String, String>): Response<String> // Ожидаем LoginResponse

    // Прочие запросы, которые могут понадобиться
    @GET("/inbox/{username}")
    suspend fun getInbox(@Path("username") username: String, @Header("X-Auth-Token") token: String): Response<List<Message>>

    @POST("/messages")
    suspend fun sendMessage(
        @Body message: Message,
        @Header("X-Auth-Token") token: String
    ): Response<MessageResponse>

    @POST("/logout")
    suspend fun logoutUser(@Header("X-Auth-Token") token: String): Response<Unit>
}
