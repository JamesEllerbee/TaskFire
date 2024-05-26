package com.jamesellerbee.taskfire.tasktrackerapi.app.dal.repository.account

import com.jamesellerbee.taskfire.tasktrackerapi.app.interfaces.AdminRepository
import com.jamesellerbee.tasktracker.lib.entities.Admin

class InMemoryAdminRepository : AdminRepository {
    private val admins = mutableMapOf<String, Admin>()
    override fun isAdmin(accountId: String): Boolean {
        return admins.containsKey(accountId)
    }

    override fun addAdmin(accountId: String) {
        admins[accountId] = Admin(accountId)
    }
}