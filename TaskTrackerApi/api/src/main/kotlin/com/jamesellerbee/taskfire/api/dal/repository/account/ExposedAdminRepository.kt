package com.jamesellerbee.taskfire.api.dal.repository.account

import com.jamesellerbee.taskfire.api.interfaces.AdminRepository
import com.jamesellerbee.taskfire.api.util.ExposedDatabaseHelper
import com.jamesellerbee.tasktracker.lib.entities.Admin
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

class ExposedAdminRepository(serviceLocator: ServiceLocator) : AdminRepository {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val database = ExposedDatabaseHelper.init(serviceLocator)

    override fun isAdmin(accountId: String): Boolean {
        var result = false

        transaction(database) {
            result = AdminEntity.find { Admins.accountId eq accountId }.any()
        }

        return result
    }

    override fun addAdmin(accountId: String) {
        transaction(database) {
            val existingAdmins = AdminEntity.find {
                (Admins.accountId eq accountId)
            }

            if (existingAdmins.empty()) {
                AdminEntity.new {
                    this.accountId = accountId
                }
            } else {
                logger.warn("There already exists an admin with this id.")
            }
        }
    }


    object Admins : IntIdTable() {
        val accountId = varchar("accountId", 50)
    }

    class AdminEntity(id: EntityID<Int>) : IntEntity(id) {
        companion object : IntEntityClass<AdminEntity>(Admins)

        var accountId by Admins.accountId

        fun toAdmin(): Admin {
            return Admin(
                accountId = accountId
            )
        }
    }
}