package com.artm44.mychats.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artm44.mychats.models.LoginResponse
import com.artm44.mychats.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class AuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    // Регистрация
    fun register(username: String) {
        viewModelScope.launch {
            try {
                // Создаем Map для запроса на регистрацию
                val registerRequest = mapOf("name" to username)

                // Запрос на регистрацию
                val response: Response<String> = RetrofitInstance.api.registerUser(username)

                if (response.isSuccessful) {
                    // Извлекаем пароль из ответа
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
                // Создаем Map для запроса на логин
                val loginRequest = mapOf("name" to username, "pwd" to password)

                // Запрос на логин
                val response: Response<String> = RetrofitInstance.api.loginUser(loginRequest)

                if (response.isSuccessful) {
                    // Возвращаем токен, если логин прошел успешно
                    val token = response.body()?: ""
                    _authState.value = AuthState.LoggedIn(token)
                } else {
                    _authState.value = AuthState.Error("Login failed: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.d("AuthViewModel", "${e.message}")
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // Состояния авторизации
    sealed class AuthState {
        object Idle : AuthState()
        data class Registered(val password: String) : AuthState()  // Используем пароль из ответа
        data class LoggedIn(val token: String) : AuthState()
        data class Error(val message: String) : AuthState()
    }
}
