package com.jamesellerbee.taskfire.api.dal.repository.account

import com.jamesellerbee.taskfire.api.interfaces.AccountRepository
import com.jamesellerbee.taskfire.api.util.ExposedDatabaseHelper
import com.jamesellerbee.tasktracker.lib.entities.Account
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

class ExposedAccountRepository(serviceLocator: ServiceLocator) : AccountRepository {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val database = ExposedDatabaseHelper.init(serviceLocator)

    override fun addAccount(newAccount: Account) {
        transaction(database) {
            AccountEntity.find { Accounts.name eq newAccount.name }.firstOrNull()?.delete()
            AccountEntity.new {
                name = newAccount.name
                email = newAccount.email
                password = newAccount.password
                accountId = newAccount.id
                created = newAccount.created
                verified = when (newAccount.verified) {
                    true -> 1
                    false -> 0
                }
            }
        }
    }

    override fun deleteAccount(accountId: String) {
        transaction(database) {
            AccountEntity.find { Accounts.accountId eq accountId }.firstOrNull()?.delete()
        }
    }

    override fun getAccounts(): List<Account> {
        val accounts = mutableListOf<Account>()

        transaction(database) {
            AccountEntity.all().forEachIndexed { _, accountEntity ->
                accounts.add(accountEntity.toAccount())
            }
        }

        return accounts.toList()
    }

    override fun getAccount(accountId: String): Account? {
        var account: Account? = null

        transaction(database) {
            AccountEntity.find { Accounts.accountId eq accountId }.firstOrNull()?.let { accountEntity ->
                account = accountEntity.toAccount()
            }
        }

        return account
    }

    object Accounts : IntIdTable() {
        val email = varchar("email", 256)
        val name = varchar("name", 50)
        val password = varchar("password", 256)
        val accountId = varchar("accountId", 50)
        val created = long("created")
        val verified = integer("verified")
    }

    class AccountEntity(id: EntityID<Int>) : IntEntity(id) {
        companion object : IntEntityClass<AccountEntity>(Accounts)

        var email by Accounts.email
        var name by Accounts.name
        var password by Accounts.password
        var accountId by Accounts.accountId
        var created by Accounts.created
        var verified by Accounts.verified

        fun toAccount(): Account {
            return Account(
                name = name,
                email = email,
                password = password,
                id = accountId,
                created = created,
                verified = verified == 1
            )
        }
    }
}