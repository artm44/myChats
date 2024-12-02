package com.artm44.mychats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.artm44.mychats.ui.theme.MyChatsTheme
import com.artm44.mychats.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyChatsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AuthScreen(authViewModel)
                }
            }
        }
    }
}

@Composable
fun AuthScreen(authViewModel: AuthViewModel) {
    val authState by authViewModel.authState.collectAsState()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { authViewModel.register(username) }) {
                Text("Register")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { authViewModel.login(username, password) }) {
                Text("Login")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        when (authState) {
            is AuthViewModel.AuthState.Registered -> Text("Registered! Password: ${(authState as AuthViewModel.AuthState.Registered).password}")
            is AuthViewModel.AuthState.LoggedIn -> Text("Logged in! Token: ${(authState as AuthViewModel.AuthState.LoggedIn).token}")
            is AuthViewModel.AuthState.Error -> Text("Error: ${(authState as AuthViewModel.AuthState.Error).message}")
            else -> {}
        }
    }
}
