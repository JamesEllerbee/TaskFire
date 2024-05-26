package com.jamesellerbee.taskfire.api.util

import com.jamesellerbee.taskfire.api.dal.properties.ApplicationProperties
import com.jamesellerbee.taskfire.api.dal.repository.DatabaseType
import com.jamesellerbee.taskfire.api.dal.repository.account.ExposedAccountRepository
import com.jamesellerbee.taskfire.api.dal.repository.account.ExposedAdminRepository
import com.jamesellerbee.taskfire.api.dal.repository.task.ExposedTaskRepository
import com.jamesellerbee.tasktracker.lib.util.ResolutionStrategy
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator
import java.io.File
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

object ExposedDatabaseHelper {
    private var database: Database? = null
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun init(serviceLocator: ServiceLocator): Database {
        if (database == null) {
            val applicationProperties = serviceLocator.resolve<ApplicationProperties>(
                ResolutionStrategy.ByType(
                    type = ApplicationProperties::class
                )
            )!!

            val databaseType = DatabaseType.valueOf(
                applicationProperties.get("databaseDriver", "")
            )

            when (databaseType) {
                DatabaseType.SQLITE -> {
                    val sqliteDbPath = applicationProperties.get(
                        "sqliteDbPath",
                        ""
                    )



                    // Create sqlite db file if it doesn't already exist
                    val file = File(sqliteDbPath)

                    // Back up existing sqlite db file if it exists
                    if(file.exists()) {
                        val sqliteBackupPath = applicationProperties.get(
                            "sqliteDpBackupPath",
                            ""
                        )

                        logger.info("Backing up sqlite DB")
                        val backUpFile = File(sqliteBackupPath)
                        backUpFile.parentFile.mkdirs()
                        file.copyTo(backUpFile, true)
                    } else {
                        logger.info("Creating sqlite DB")
                        file.parentFile.mkdirs()
                        file.createNewFile()
                    }

                    database = Database.connect(
                        "jdbc:sqlite:$sqliteDbPath", "org.sqlite.JDBC"
                    )

                    transaction(database) {
                        addLogger(StdOutSqlLogger)

                        SchemaUtils.create(ExposedAccountRepository.Accounts)
                        SchemaUtils.create(ExposedTaskRepository.Tasks)
                        SchemaUtils.create(ExposedAdminRepository.Admins)

                        AccountsSqliteMigrationHelper.migrate(this)
                    }
                }
            }
        }

        return database!!
    }
}

object AccountsSqliteMigrationHelper {
    fun migrate(transaction: Transaction) {
        val logger = LoggerFactory.getLogger(this::class.java)
        logger.info("Migrating Accounts table")

        val existingColumns = transaction.exec("PRAGMA table_info(Accounts);") {
            val columns = mutableListOf<String>()
            while (it.next()) {
                columns.add(it.getString(2))
            }

            columns.toList()
        } ?: emptyList()

        if (existingColumns.none { it == "created" }) {
            transaction.exec("ALTER TABLE Accounts ADD COLUMN created BIGINT DEFAULT 0;")
        } else {
            logger.debug("Accounts table already has column \"created\". Nothing to do.")
        }

        if (existingColumns.none { it == "email" }) {
            transaction.exec("ALTER TABLE Accounts ADD COLUMN email varchar(256) DEFAULT \"\";")
        } else {
            logger.debug("Accounts tablet already has column \"email\". Nothing to do.")
        }

        if (existingColumns.none { it == "verified" }) {
            transaction.exec("ALTER TABLE Accounts ADD COLUMN verified INTEGER DEFAULT 0;")
        } else {
            logger.debug("Accounts table already has column \"verified\". Nothing to do.")
        }
    }
}