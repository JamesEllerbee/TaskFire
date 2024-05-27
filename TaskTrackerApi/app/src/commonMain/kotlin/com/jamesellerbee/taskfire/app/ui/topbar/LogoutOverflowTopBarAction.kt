package com.jamesellerbee.taskfire.app.ui.topbar

import com.jamesellerbee.taskfire.app.bl.LogoutUseCase
import com.jamesellerbee.taskfire.app.interfaces.OverflowTopBarAction
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator

class LogoutOverflowTopBarAction(
    serviceLocator: ServiceLocator,
    val logoutUseCase: LogoutUseCase = LogoutUseCase(serviceLocator)
) : OverflowTopBarAction {
    override val text: String = "Logout"

    override val onClick: () -> Unit = {
        logoutUseCase()
    }
}