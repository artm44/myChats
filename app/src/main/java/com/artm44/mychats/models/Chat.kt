package com.artm44.mychats.models

data class Chat(val name: String, val isChannel: Boolean, var lastKnownId: Int = Int.MAX_VALUE)