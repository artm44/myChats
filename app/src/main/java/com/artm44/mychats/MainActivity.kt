package com.artm44.mychats

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import com.artm44.mychats.data.ChatRepository
import com.artm44.mychats.models.Chat
import com.artm44.mychats.models.Message
import com.artm44.mychats.models.MessageData
import com.artm44.mychats.network.RetrofitInstance
import com.artm44.mychats.network.SessionManager
import com.artm44.mychats.roomdb.AppDatabase
import com.artm44.mychats.ui.theme.MyChatsTheme
import com.artm44.mychats.viewmodel.AuthViewModel
import com.artm44.mychats.viewmodel.AuthViewModelFactory
import com.artm44.mychats.viewmodel.MainViewModel
import com.artm44.mychats.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        val database = AppDatabase.getInstance(this)
        val chatDao = database.chatDao()
        val messageDao = database.messageDao()
        val apiService = RetrofitInstance.apiService

        val chatRepository = ChatRepository(chatDao, messageDao, apiService)

        val authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(sessionManager)
        )[AuthViewModel::class.java]

        val mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(sessionManager, chatRepository)
        )[MainViewModel::class.java]

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
            MainScreen(mainViewModel)
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
    mainViewModel: MainViewModel
) {
    val state by mainViewModel.state.collectAsState()
    val selectedChatOrChannel by mainViewModel.selectedChatOrChannel.collectAsState()
    val messages by mainViewModel.messages.collectAsState()

    LaunchedEffect(Unit) {
        mainViewModel.loadChats()
    }

    BackHandler() {
        when {
            selectedChatOrChannel != null -> {
                mainViewModel.selectChat(null)
            }
            else -> {
                mainViewModel.logout()
            }
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                ChatList(
                    state = state,
                    onChatSelected = { mainViewModel.selectChat(it) }
                )
            }
            Box(modifier = Modifier.weight(2f)) {
                if (selectedChatOrChannel != null) {
                    val chatOrChannel = selectedChatOrChannel!!
                    ChatDetailScreen(
                        chat = chatOrChannel,
                        onBack = { mainViewModel.selectChat(null) },
                        messages = messages,
                        onSendMessage = { message ->
                            mainViewModel.sendMessage(chatOrChannel.name, message)
                        },
                        onLoadMoreMessages = {
                            mainViewModel.loadMoreMessages(chatOrChannel)
                        }
                    )
                } else {
                    Text(
                        stringResource(R.string.choose_chat),
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            if (selectedChatOrChannel == null) {
                ChatList(
                    state = state,
                    onChatSelected = { mainViewModel.selectChat(it) }
                )
            } else {
                val chatOrChannel = selectedChatOrChannel!!
                ChatDetailScreen(
                    chat = chatOrChannel,
                    onBack = { mainViewModel.selectChat(null) },
                    messages = messages,
                    onSendMessage = { message ->
                        mainViewModel.sendMessage(chatOrChannel.name, message)
                    },
                    onLoadMoreMessages = {
                        mainViewModel.loadMoreMessages(chatOrChannel)
                    }
                )
            }
        }
    }
}


@Composable
fun ChatList(
    state: MainViewModel.MainScreenState,
    onChatSelected: (Chat) -> Unit
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
            Text(stringResource(R.string.error, state.message), modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
fun ChatDetailScreen(
    chat: Chat,
    onBack: () -> Unit,
    messages: List<Message>,
    onSendMessage: (String) -> Unit,
    onLoadMoreMessages: suspend () -> Boolean
) {
    var inputMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var canLoadMore by remember { mutableStateOf(true) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty() && listState.firstVisibleItemIndex == 0) {
            listState.scrollToItem(0)
        }
    }

    LaunchedEffect(listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index, canLoadMore) {
        val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        if (lastVisibleIndex == messages.lastIndex && canLoadMore) {
            canLoadMore = onLoadMoreMessages()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Chat: ${chat.name}",
                style = MaterialTheme.typography.titleMedium
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState,
            reverseLayout = true
        ) {
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputMessage,
                onValueChange = { inputMessage = it },
                placeholder = { Text(stringResource(R.string.type_a_message)) },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                maxLines = 1,
                singleLine = true
            )
            Button(
                onClick = {
                    if (inputMessage.isNotBlank()) {
                        onSendMessage(inputMessage)
                        inputMessage = ""
                    }
                },
                enabled = inputMessage.isNotBlank()
            ) {
                Text(stringResource(R.string.send_button))
            }
        }
    }
}

@Composable
fun ImageMessage(
    from: String,
    imagePath: String
) {
    var showFullScreenImage by remember { mutableStateOf(false) }

    Column {
        Text("$from: ", style = MaterialTheme.typography.bodyMedium)

        AsyncImage(
            model = stringResource(R.string.img_url, "thumb/$imagePath"),
            contentDescription = "Thumbnail",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clickable { showFullScreenImage = true }
        )
    }

    if (showFullScreenImage) {
        FullScreenImageDialog(
            imageUrl = stringResource(R.string.img_url, "img/$imagePath"),
            onClose = { showFullScreenImage = false }
        )
    }
}


@Composable
fun FullScreenImageDialog(
    imageUrl: String,
    onClose: () -> Unit
) {
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Full-size image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
            )

            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(24.dp)
                    .clickable { onClose() }
            )
        }
    }
}

