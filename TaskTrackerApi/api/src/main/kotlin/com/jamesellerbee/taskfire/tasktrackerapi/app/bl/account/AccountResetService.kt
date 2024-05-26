package com.jamesellerbee.taskfire.tasktrackerapi.app.bl.account

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ResetKey(
    val resetKey: String,
    val timeStamp: Long
)

class AccountResetService {
    private val resetMap = mutableMapOf<String, ResetKey>()

    init {
        CoroutineScope(SupervisorJob()).launch {
            while (isActive) {
                resetMap.entries.filter { entry ->
                    entry.value.timeStamp <= System.currentTimeMillis() - 1.2e+6
                }
                    .forEach { entry ->
                        resetMap.remove(entry.key)
                    }

                delay(10000)
            }
        }
    }

    fun putResetKey(accountId: String, resetKey: String) {
        resetMap[accountId] = ResetKey(resetKey, System.currentTimeMillis())
    }

    fun getKey(accountId: String): String? {
        return resetMap[accountId]?.resetKey
    }
}