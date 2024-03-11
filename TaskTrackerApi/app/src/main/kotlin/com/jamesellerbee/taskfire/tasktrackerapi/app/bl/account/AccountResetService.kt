package com.jamesellerbee.taskfire.tasktrackerapi.app.bl.account

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
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
                resetMap.entries.forEach { entry ->
                    if (entry.value.timeStamp <= System.currentTimeMillis() - 1.2e+6) {
                        resetMap.remove(entry.key)
                    }
                }
            }
        }
    }
}