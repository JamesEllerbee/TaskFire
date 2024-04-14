package com.jamesellerbee.tasktracker.lib.entities

import kotlinx.serialization.Serializable

@Serializable
data class Account(
    val name: String,
    val email: String = "",
    val password: String = "",
    val id: String = "",
    val created: Long = 0,
    val verified: Boolean = false,
)

@Serializable
data class Email(val email: String)

data class Password(val password: String)