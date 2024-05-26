package com.jamesellerbee.taskfire.tasktrackerapi.app.dal.repository.account

import com.jamesellerbee.taskfire.tasktrackerapi.app.interfaces.AccountRepository
import com.jamesellerbee.tasktracker.lib.entities.Account

class InMemoryAccountRepository : AccountRepository {
    private val accounts = mutableMapOf<String, Account>()

    override fun addAccount(newAccount: Account) {
        accounts[newAccount.id] = newAccount
    }

    override fun deleteAccount(accountId: String) {
        accounts.remove(accountId)
    }

    override fun getAccounts(): List<Account> {
        return accounts.values.toList()
    }

    override fun getAccount(accountId: String): Account? {
        return accounts[accountId]
    }
}