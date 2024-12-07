package com.artm44.mychats.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.artm44.mychats.network.RetrofitInstance
import com.artm44.mychats.network.SessionManager
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull

class AuthViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            
            val storedToken = sessionManager.token.firstOrNull()
            val storedUsername = sessionManager.username.firstOrNull()

            if (!storedToken.isNullOrEmpty() && !storedUsername.isNullOrEmpty()) {
                Log.d("AuthView", storedToken)
                _authState.value = AuthState.LoggedIn(storedToken)
            } else {
                _authState.value = AuthState.Idle
            }

            // Подписываемся на изменения токена и username
            sessionManager.token.combine(sessionManager.username) { token, username ->
                token to username
            }.collect { (token, username) ->
                if (token.isNullOrEmpty() || username.isNullOrEmpty()) {
                    _authState.value = AuthState.Idle
                }
            }
        }
    }



    
    fun register(username: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiService.registerUser(username)
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

    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val loginRequest = mapOf("name" to username, "pwd" to password)
                val response = RetrofitInstance.apiService.loginUser(loginRequest)
                if (response.isSuccessful) {
                    val token = response.body() ?: ""
                    sessionManager.saveSession(token, username) 
                    Log.d("AuthViewModel login", token)
                    Log.d("AuthViewModel login", username)
                    _authState.value = AuthState.LoggedIn(token) 
                } else {
                    _authState.value = AuthState.Error("Login failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }


    
    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
            _authState.value = AuthState.Idle
        }
    }

    sealed class AuthState {
        object Idle : AuthState()
        data class Registered(val password: String) : AuthState()
        data class LoggedIn(val token: String) : AuthState()
        data class Error(val message: String) : AuthState()
    }
}

