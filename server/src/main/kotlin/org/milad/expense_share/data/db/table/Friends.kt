package org.milad.expense_share.data.db.table

import org.jetbrains.exposed.sql.Table
import org.milad.expense_share.data.models.FriendRelationStatus

object Friends : Table("friends") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id") references Users.id
    val friendId = integer("friend_id") references Users.id
    val status = enumerationByName("status", 20, FriendRelationStatus::class)
    override val primaryKey = PrimaryKey(id)
}