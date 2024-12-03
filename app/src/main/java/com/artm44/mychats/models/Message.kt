package com.artm44.mychats.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Message(
    val id: String,
    val from: String,
    val to: String,
    val data: MessageData,
    val time: String?
)

@JsonClass(generateAdapter = true)
sealed class MessageData {
    data class Text(val text: String) : MessageData()
    data class Image(val link: String?) : MessageData()
}
