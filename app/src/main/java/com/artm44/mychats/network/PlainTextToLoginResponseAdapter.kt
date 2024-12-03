package com.artm44.mychats.network

import com.artm44.mychats.models.LoginResponse
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

class PlainTextToLoginResponseAdapter : JsonAdapter<LoginResponse>() {
    @FromJson
    override fun fromJson(reader: JsonReader): LoginResponse? {
        return if (reader.peek() == JsonReader.Token.STRING) {
            LoginResponse(token = reader.nextString())
        } else {
            null
        }
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: LoginResponse?) {
        writer.value(value?.token)
    }
}
