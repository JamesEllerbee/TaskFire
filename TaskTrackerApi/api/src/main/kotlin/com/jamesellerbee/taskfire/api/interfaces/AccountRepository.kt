package com.jamesellerbee.taskfire.api.interfaces

import com.jamesellerbee.tasktracker.lib.entities.Account

interface AccountRepository {
    fun addAccount(newAccount: Account)

    fun deleteAccount(accountId: String)

    fun getAccounts(): List<Account>

    fun getAccount(accountId: String): Account?
}