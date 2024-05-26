package com.jamesellerbee.taskfire.app.ui.admin.dashboard

import com.jamesellerbee.taskfire.app.dal.rest.Request
import com.jamesellerbee.taskfire.app.dal.rest.RestRequestService
import com.jamesellerbee.tasktracker.lib.entities.Account
import com.jamesellerbee.tasktracker.lib.util.ResolutionStrategy
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminDashboardViewModel(serviceLocator: ServiceLocator) {
    private val restRequestService by serviceLocator.resolveLazy<RestRequestService>(
        ResolutionStrategy.ByType(
            RestRequestService::class
        )
    )

    private val _isFetchingAccounts = MutableStateFlow(false)
    val isFetchingAccounts = _isFetchingAccounts.asStateFlow()

    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts = _accounts.asStateFlow()

    init {
        fetchAccounts()
    }

    fun deleteAccount(account: Account, onDelete: (Boolean) -> Unit) {
        restRequestService.enqueueRequest(Request.DeleteAccount(account.id) { success ->
            onDelete(success)

            if (success) {
                fetchAccounts()
            }
        })
    }

    fun updateAccount(account: Account, onUpdate: (Boolean) -> Unit) {
        restRequestService.enqueueRequest(Request.UpdateAccount(account) { success ->
            onUpdate(success)
        })
    }

    fun generateTemporaryPassword(length: Int = 12): String {
        val characterMap = "abcdefghijklmnopqrstuvwxyz1234567890-=!@#$%^&*()_+"

        val temporaryPassword = StringBuilder()

        repeat(length) {
            val randomIndex = Random.nextInt(characterMap.length)
            temporaryPassword.append(characterMap[randomIndex])
        }

        return temporaryPassword.toString()
    }

    private fun fetchAccounts() {
        _isFetchingAccounts.value = true
        restRequestService.enqueueRequest(Request.GetAccounts {
            _accounts.value = it
            _isFetchingAccounts.value = false
        })
    }


}