package com.artm44.mychats.models

data class Message(
    val to: String = "1@channel", // по умолчанию канал
    val data: MessageData
)

data class MessageData(
    val text: MessageText
)

data class MessageText(
    val text: String
)

data class MessageResponse(
    val id: String  // Идентификатор отправленного сообщения
)