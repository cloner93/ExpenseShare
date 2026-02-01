package org.milad.expense_share.data.db.table

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object FriendRelations : Table("friend_relations") {
    val userId = integer("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val friendId = integer("friend_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val status = varchar("status", 20)
    val requestedBy = integer("requested_by").references(Users.id)
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")

    override val primaryKey = PrimaryKey(userId, friendId)

    init {
        index(isUnique = false, status)
        index(isUnique = false, userId)
        index(isUnique = false, friendId)
    }
}