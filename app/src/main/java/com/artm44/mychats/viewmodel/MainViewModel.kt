package com.artm44.mychats.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artm44.mychats.models.Message
import com.artm44.mychats.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val api = RetrofitInstance.apiService

    private var token: String = ""
    private var username: String = ""

    private val _state = MutableStateFlow<MainScreenState>(MainScreenState.Loading)
    val state = _state.asStateFlow()

    private val _selectedChatOrChannel = MutableStateFlow<ChatOrChannel?>(null)
    val selectedChatOrChannel = _selectedChatOrChannel.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    fun setCredentials(userToken: String, userName: String) {
        token = userToken
        username = userName
    }

    fun loadChatsAndChannels() {
        viewModelScope.launch {
            try {
                // Получение каналов
                val channelsResponse = api.getChannels(token)
                val channels = channelsResponse.body() ?: emptyList()

                // Получение сообщений
                val messagesResponse = api.getInbox(username, token)
                val messages = messagesResponse.body() ?: emptyList()

                // Фильтрация чатов (исключая каналы)
                val chats = messages
                    .filter { it.to != null && !it.to!!.contains("@channel") }
                    .map { message ->
                        val otherParty = if (message.from == username) message.to!! else message.from
                        ChatOrChannel(otherParty, isChannel = false)
                    }
                    .distinctBy { it.name }

                // Объединение чатов и каналов
                val allItems = chats + channels.map { ChatOrChannel(it, isChannel = true) }
                _state.value = MainScreenState.Success(allItems)
            } catch (e: Exception) {
                Log.d("MainViewModel", e.message ?: "Unknown error")
                _state.value = MainScreenState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun loadMessagesForChatOrChannel(chatOrChannel: ChatOrChannel) {
        viewModelScope.launch {
            try {
                val messagesResponse = if (chatOrChannel.isChannel) {
                    api.getChannelMessages(chatOrChannel.name, token)
                } else {
                    api.getInbox(username, token) // Загружаем все сообщения для пользователя
                }

                val allMessages = messagesResponse.body() ?: emptyList()
                // Фильтруем сообщения, если это не канал
                _messages.value = if (!chatOrChannel.isChannel) {
                    allMessages.filter { it.from == chatOrChannel.name || it.to == chatOrChannel.name }
                } else {
                    allMessages
                }
            } catch (e: Exception) {
                Log.d("MainViewModel", e.message ?: "Unknown error")
                _messages.value = emptyList()
            }
        }
    }

    fun selectChatOrChannel(item: ChatOrChannel?) {
        _selectedChatOrChannel.value = item
        if (item != null) {
            loadMessagesForChatOrChannel(item)
        } else {
            _messages.value = emptyList()
        }
    }

    sealed class MainScreenState {
        object Loading : MainScreenState()
        data class Success(val chatsAndChannels: List<ChatOrChannel>) : MainScreenState()
        data class Error(val message: String) : MainScreenState()
    }

    data class ChatOrChannel(val name: String, val isChannel: Boolean)
}

