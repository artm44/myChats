package com.artm44.mychats.network

import com.artm44.mychats.models.MessageData
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class MessageDataAdapter {

    @FromJson
    fun fromJson(json: Map<String, Map<String, String>>): MessageData {
        return when {
            "Text" in json -> {
                val textData = json["Text"] ?: throw IllegalArgumentException("Missing Text data")
                MessageData.Text(textData["text"] ?: throw IllegalArgumentException("Text field is missing"))
            }
            "Image" in json -> {
                val imageData = json["Image"] ?: throw IllegalArgumentException("Missing Image data")
                MessageData.Image(imageData["link"] ?: "")
            }
            else -> throw IllegalArgumentException("Unknown MessageData type")
        }
    }

    @ToJson
    fun toJson(data: MessageData): Map<String, Map<String, String?>> {
        return when (data) {
            is MessageData.Text -> mapOf("Text" to mapOf("text" to data.text))
            is MessageData.Image -> mapOf("Image" to mapOf("link" to data.link))
        }
    }
}
