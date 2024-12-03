package com.artm44.mychats

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.artm44.mychats.models.Message
import com.artm44.mychats.models.MessageData
import com.artm44.mychats.ui.theme.MyChatsTheme
import com.artm44.mychats.viewmodel.AuthViewModel
import com.artm44.mychats.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyChatsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppScreen(authViewModel, mainViewModel)
                }
            }
        }
    }
}

@Composable
fun AppScreen(authViewModel: AuthViewModel, mainViewModel: MainViewModel) {
    val authState by authViewModel.authState.collectAsState()

    when (authState) {
        is AuthViewModel.AuthState.LoggedIn -> {
            val token = (authState as AuthViewModel.AuthState.LoggedIn).token
            MainScreen(mainViewModel, token, "artm44") // TODO: change input
        }
        else -> {
            AuthScreen(authViewModel)
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

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    token: String,
    username: String
) {
    val state by mainViewModel.state.collectAsState()
    val selectedChatOrChannel by mainViewModel.selectedChatOrChannel.collectAsState()
    val messages by mainViewModel.messages.collectAsState()

    // Устанавливаем токен и имя пользователя в ViewModel
    LaunchedEffect(Unit) {
        mainViewModel.setCredentials(token, username)
        mainViewModel.loadChatsAndChannels()
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        // Горизонтальная ориентация
        Row(modifier = Modifier.fillMaxSize()) {
            // Список чатов
            Box(modifier = Modifier.weight(1f)) {
                ChatList(
                    state = state,
                    onChatSelected = { mainViewModel.selectChatOrChannel(it) }
                )
            }
            // Детали чата
            Box(modifier = Modifier.weight(2f)) {
                if (selectedChatOrChannel != null) {
                    ChatDetailScreen(
                        chatOrChannel = selectedChatOrChannel!!,
                        onBack = { mainViewModel.selectChatOrChannel(null) },
                        messages = messages
                    )
                } else {
                    Text(
                        "Выберите чат",
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    } else {
        // Портретная ориентация
        Box(modifier = Modifier.fillMaxSize()) {
            if (selectedChatOrChannel == null) {
                // Список чатов
                ChatList(
                    state = state,
                    onChatSelected = { mainViewModel.selectChatOrChannel(it) }
                )
            } else {
                // Детали чата
                ChatDetailScreen(
                    chatOrChannel = selectedChatOrChannel!!,
                    onBack = { mainViewModel.selectChatOrChannel(null) },
                    messages = messages
                )
            }
        }
    }
}


@Composable
fun ChatList(
    state: MainViewModel.MainScreenState,
    onChatSelected: (MainViewModel.ChatOrChannel) -> Unit
) {
    when (state) {
        is MainViewModel.MainScreenState.Loading -> {
            CircularProgressIndicator(modifier = Modifier.fillMaxSize())
        }
        is MainViewModel.MainScreenState.Success -> {
            val items = state.chatsAndChannels
            LazyColumn {
                items(items) { item ->
                    ListItem(
                        modifier = Modifier.clickable { onChatSelected(item) },
                        headlineContent = { Text(item.name) }
                    )
                }
            }
        }
        is MainViewModel.MainScreenState.Error -> {
            Text("Error: ${state.message}", modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
fun ChatDetailScreen(
    chatOrChannel: MainViewModel.ChatOrChannel,
    onBack: () -> Unit,
    messages: List<Message>
) {
    Column {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text(
            text = "Chat/Channel: ${chatOrChannel.name}",
            style = MaterialTheme.typography.titleMedium
        )
        LazyColumn {
            items(messages) { message ->
                when (val data = message.data) {
                    is MessageData.Text -> Text("${message.from}: ${data.text}")
                    is MessageData.Image -> {
                        data.link?.let {
                            ImageMessage(
                                from = message.from,
                                imagePath = it
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImageMessage(
    from: String,
    imagePath: String
) {
    val fullImageUrl = "https://faerytea.name:8008/img/$imagePath"
    val thumbnailUrl = "https://faerytea.name:8008/thumb/$imagePath"

    // Состояние для отображения полноэкранного изображения
    var showFullScreenImage by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(8.dp)) {
        Text("$from: ", style = MaterialTheme.typography.bodyMedium)

        // Кликабельное мини-изображение
        AsyncImage(
            model = thumbnailUrl,
            contentDescription = "Thumbnail",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clickable { showFullScreenImage = true } // Показать полноэкранное изображение при клике
        )
    }

    // Открытие изображения в диалоге при клике
    if (showFullScreenImage) {
        FullScreenImageDialog(
            imageUrl = fullImageUrl,
            onClose = { showFullScreenImage = false }
        )
    }
}


@Composable
fun FullScreenImageDialog(
    imageUrl: String,
    onClose: () -> Unit
) {
    // Диалог с полноэкранным изображением
    Dialog(onDismissRequest = onClose) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black) // Темный фон
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Full-size image",
                contentScale = ContentScale.FillWidth, // Используем FillWidth, чтобы изображение растягивалось по ширине экрана
                modifier = Modifier
                    .fillMaxSize() // Заполняем весь экран без отступов
            )

            // Закрытие диалога при клике
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onClose() } // Закрытие при клике
            )
        }
    }
}
