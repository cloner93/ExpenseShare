package org.milad.expense_share.data

import org.jetbrains.exposed.sql.ResultRow
import org.milad.expense_share.data.db.table.Users
import org.milad.expense_share.data.models.User

fun ResultRow.toUser() = User(
    id = this[Users.id],
    username = this[Users.username],
    phone = this[Users.phone]
)