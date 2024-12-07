package com.artm44.mychats.models

import com.artm44.mychats.roomdb.MessageEntity
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Message(
    val id: Int,
    val from: String,
    val to: String,
    val data: MessageData,
    val time: String?
) {
    fun toEntity(): MessageEntity {
        return MessageEntity(
            id = this.id,
            from = this.from,
            to = this.to,
            text = (this.data as? MessageData.Text)?.text, 
            imageLink = (this.data as? MessageData.Image)?.link, 
            time = this.time.orEmpty() 
        )
    }

}

@JsonClass(generateAdapter = true)
data class MessageRequest(
    val from: String,
    val to: String,
    val data: MessageData
)

@JsonClass(generateAdapter = true)
sealed class MessageData {
    data class Text(val text: String) : MessageData()
    data class Image(val link: String?) : MessageData()
}
