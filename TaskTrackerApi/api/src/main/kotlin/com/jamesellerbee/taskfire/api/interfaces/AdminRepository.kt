package com.jamesellerbee.taskfire.api.interfaces

interface AdminRepository {
    fun isAdmin(accountId: String): Boolean

    fun addAdmin(accountId: String)
}