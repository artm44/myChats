package com.artm44.mychats.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import com.artm44.mychats.network.RetrofitInstance

class AuthViewModel : ViewModel() {

    private val api = RetrofitInstance.apiService

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    // Регистрация
    fun register(username: String) {
        viewModelScope.launch {
            try {
                val response: Response<String> = api.registerUser(username)

                if (response.isSuccessful) {
                    val password = response.body()?.substringAfter("password: '")?.substringBefore("'") ?: "Unknown password"
                    _authState.value = AuthState.Registered(password)
                } else {
                    _authState.value = AuthState.Error("Registration failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // Логин
    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val loginRequest = mapOf("name" to username, "pwd" to password)
                val response: Response<String> = api.loginUser(loginRequest)
                if (response.isSuccessful) {
                    val token = response.body() ?: ""
                    _authState.value = AuthState.LoggedIn(token)
                } else {
                    _authState.value = AuthState.Error("Login failed: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.d("AuthViewModel", e.message ?: "Unknown error")
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }

    sealed class AuthState {
        object Idle : AuthState()
        data class Registered(val password: String) : AuthState()
        data class LoggedIn(val token: String) : AuthState()
        data class Error(val message: String) : AuthState()
    }
}
