package com.artm44.mychats.models

import com.squareup.moshi.Json

data class RegisterResponse(
    val password: String
)

data class LoginResponse(
    val password: String
)

data class LoginData(
    val name: String,
    val password: String
)