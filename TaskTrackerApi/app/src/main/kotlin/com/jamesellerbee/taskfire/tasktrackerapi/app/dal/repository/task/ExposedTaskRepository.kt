package com.jamesellerbee.taskfire.tasktrackerapi.app.dal.repository.task

import com.jamesellerbee.taskfire.tasktrackerapi.app.dal.entites.Task
import com.jamesellerbee.taskfire.tasktrackerapi.app.interfaces.TaskRepository
import com.jamesellerbee.taskfire.tasktrackerapi.app.util.ExposedDatabaseHelper
import com.jamesellerbee.taskfire.tasktrackerapi.app.util.ServiceLocator
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class ExposedTaskRepository(serviceLocator: ServiceLocator) : TaskRepository {
    private val database = ExposedDatabaseHelper.init(serviceLocator)

    override fun getTasksByAccountId(accountId: String): List<Task> {
        val tasks = mutableListOf<Task>()

        transaction(database) {
            TaskEntity.find { Tasks.accountId eq accountId }.forEachIndexed { _, taskEntity ->
                tasks.add(taskEntity.toTask())
            }
        }

        return tasks.toList()
    }

    override fun addTask(accountId: String, task: Task) {
        transaction {
            TaskEntity.find { Tasks.taskId eq task.taskId }.firstOrNull()?.delete()
            TaskEntity.new {
                title = task.title
                this.accountId = task.accountId
                created = task.created
                modified = task.modified
                completed = task.completed
                description = task.description
                tasksId = task.taskId
            }
        }
    }

    override fun removeTask(accountId: String, taskId: String) {
        TaskEntity.find { (Tasks.taskId eq taskId) and (Tasks.accountId eq accountId) }
    }

    object Tasks : IntIdTable() {
        val title = varchar("tile", 256)
        val accountId = varchar("accountId", 256)
        val created = long("created")
        val modified = long("modified")
        val completed = bool("completed")
        val description = varchar("description", 256)
        val taskId = varchar("taskId", 256)
    }

    class TaskEntity(id: EntityID<Int>) : IntEntity(id) {
        companion object : IntEntityClass<TaskEntity>(Tasks)

        var title by Tasks.title
        var accountId by Tasks.accountId
        var created by Tasks.created
        var modified by Tasks.modified
        var completed by Tasks.completed
        var description by Tasks.description
        var tasksId by Tasks.taskId

        fun toTask(): Task {
            return Task(
                title = title,
                accountId = accountId,
                created = created,
                modified = modified,
                completed = completed,
                description = description,
                taskId = tasksId
            )
        }
    }
}