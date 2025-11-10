package org.milad.expense_share.data.db.table

import org.jetbrains.exposed.sql.Table

object Passwords : Table("passwords") {
    val userId = integer("user_id") references Users.id
    val hash = varchar("hash", 255)
}