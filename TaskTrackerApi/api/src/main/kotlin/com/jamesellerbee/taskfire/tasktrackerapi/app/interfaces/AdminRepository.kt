package com.jamesellerbee.taskfire.tasktrackerapi.app.interfaces

interface AdminRepository {
    fun isAdmin(accountId: String): Boolean

    fun addAdmin(accountId: String)
}