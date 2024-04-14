package com.jamesellerbee.taskfire.tasktrackerapi.app.dal.repository.task

import com.jamesellerbee.taskfire.tasktrackerapi.app.interfaces.TaskRepository
import com.jamesellerbee.tasktracker.lib.entities.Task

class InMemoryTaskRepository : TaskRepository {
    private val taskMap = mutableMapOf<String, MutableList<Task>>()

    override fun getTasks(): List<Task> {
        return taskMap.values.flatMap { it.toList() }
    }

    override fun getTasksByAccountId(accountId: String): List<Task> {
        return taskMap[accountId] ?: emptyList()
    }

    override fun addTask(accountId: String, task: Task) {
        val tasks = taskMap[accountId]
        tasks?.removeIf { it.taskId == task.taskId }
        taskMap.computeIfAbsent(accountId) { mutableListOf() }.add(task)
    }

    override fun removeTask(accountId: String, taskId: String) {
        val tasks = taskMap[accountId]
        tasks?.removeIf { it.taskId == taskId }
    }
}