package com.artm44.mychats.roomdb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: Int,
    val from: String,
    val to: String,
    val text: String?,
    val imageLink: String?,
    val time: String
)