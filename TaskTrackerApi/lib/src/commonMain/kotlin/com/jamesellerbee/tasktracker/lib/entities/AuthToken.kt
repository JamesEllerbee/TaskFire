package com.jamesellerbee.tasktracker.lib.entities

import kotlinx.serialization.Serializable

@Serializable
data class AuthToken(
    val sessionId: String = "",
    val accountId: String = "",
    val timeStamp: Long = 0
)
