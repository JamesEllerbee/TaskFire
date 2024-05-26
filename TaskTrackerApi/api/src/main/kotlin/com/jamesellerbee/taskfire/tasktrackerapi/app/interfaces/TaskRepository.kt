package com.jamesellerbee.taskfire.tasktrackerapi.app.interfaces

import com.jamesellerbee.tasktracker.lib.entities.Task

interface TaskRepository {
    fun getTasksByAccountId(accountId: String): List<Task>

    fun getTasks(): List<Task>

    fun addTask(accountId: String, task: Task)

    fun removeTask(accountId: String, taskId: String)
}