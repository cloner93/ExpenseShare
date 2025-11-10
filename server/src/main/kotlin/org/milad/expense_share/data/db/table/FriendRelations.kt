package org.milad.expense_share.data.db.table

import org.jetbrains.exposed.sql.Table

object FriendRelations : Table("friend_relations") {
    val userId = integer("user_id") references Users.id
    val friendId = integer("friend_id") references Users.id
    val status = varchar("status", 20)
}