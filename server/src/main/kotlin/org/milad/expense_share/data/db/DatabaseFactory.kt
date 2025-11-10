package org.milad.expense_share.data.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.milad.expense_share.data.db.table.FriendRelations
import org.milad.expense_share.data.db.table.Friends
import org.milad.expense_share.data.db.table.GroupMembers
import org.milad.expense_share.data.db.table.Groups
import org.milad.expense_share.data.db.table.Passwords
import org.milad.expense_share.data.db.table.Transactions
import org.milad.expense_share.data.db.table.Users

object DatabaseFactory {

    fun init() {
        val db = Database.connect(hikari())

        transaction(db) {
            SchemaUtils.create(
                Users,
                Passwords,
                Friends,
                FriendRelations,
                Groups,
                GroupMembers,
                Transactions
            )
        }
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = "jdbc:postgresql://localhost:5432/expenseshare"
            username = "postgres"
            password = "miladmilad"

            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        return HikariDataSource(config)
    }
}