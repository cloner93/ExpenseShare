package org.milad.expense_share.data.repository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.milad.expense_share.data.db.table.Settlements
import org.milad.expense_share.data.db.table.Users
import org.milad.expense_share.data.models.Settlement
import org.milad.expense_share.data.models.SettlementStatus
import org.milad.expense_share.data.models.User
import org.milad.expense_share.domain.repository.SettlementRepository

class SettlementRepositoryImpl : SettlementRepository {

    override fun getByGroup(groupId: Int): List<Settlement> = transaction {
        val debtorAlias = Users.alias("debtor_user")
        val creditorAlias = Users.alias("creditor_user")

        Settlements
            .join(
                debtorAlias, JoinType.INNER,
                Settlements.debtorId, debtorAlias[Users.id]
            )
            .join(
                creditorAlias, JoinType.INNER,
                Settlements.creditorId, creditorAlias[Users.id]
            )
            .selectAll()
            .where { Settlements.groupId eq groupId }
            .map { row ->
                Settlement(
                    id = row[Settlements.id],
                    groupId = row[Settlements.groupId],
                    debtor = User(
                        id = row[debtorAlias[Users.id]],
                        username = row[debtorAlias[Users.username]],
                        phone = row[debtorAlias[Users.phone]]
                    ),
                    creditor = User(
                        id = row[creditorAlias[Users.id]],
                        username = row[creditorAlias[Users.username]],
                        phone = row[creditorAlias[Users.phone]]
                    ),
                    amount = row[Settlements.amount],
                    status = row[Settlements.status],
                    createdAt = row[Settlements.createdAt],
                    updatedAt = row[Settlements.updatedAt],
                )
            }
    }

    override fun replaceAll(groupId: Int, settlements: List<Settlement>): Unit = transaction {
        // delete the oldest
        Settlements.deleteWhere { Settlements.groupId eq groupId }

        // insert new
        if (settlements.isEmpty()) return@transaction

        val now = System.currentTimeMillis()
        Settlements.batchInsert(settlements) { s ->
            this[Settlements.groupId] = groupId
            this[Settlements.debtorId] = s.debtor.id
            this[Settlements.creditorId] = s.creditor.id
            this[Settlements.amount] = s.amount
            this[Settlements.status] = SettlementStatus.PENDING
            this[Settlements.createdAt] = now
            this[Settlements.updatedAt] = now
        }
    }

    override fun markAsPaid(settlementId: Int, actorId: Int): Boolean = transaction {
        val row = Settlements.selectAll()
            .where { Settlements.id eq settlementId }
            .singleOrNull() ?: return@transaction false

        if (row[Settlements.debtorId] != actorId) return@transaction false

        if (row[Settlements.status] != SettlementStatus.PENDING) return@transaction false

        Settlements.update({ Settlements.id eq settlementId }) {
            it[status] = SettlementStatus.PAID
            it[updatedAt] = System.currentTimeMillis()
        } > 0
    }

    override fun confirm(settlementId: Int, actorId: Int): Boolean = transaction {
        val row = Settlements.selectAll()
            .where { Settlements.id eq settlementId }
            .singleOrNull() ?: return@transaction false

        if (row[Settlements.creditorId] != actorId) return@transaction false

        if (row[Settlements.status] != SettlementStatus.PAID) return@transaction false

        Settlements.update({ Settlements.id eq settlementId }) {
            it[status] = SettlementStatus.CONFIRMED
            it[updatedAt] = System.currentTimeMillis()
        } > 0
    }

    override fun dispute(settlementId: Int, actorId: Int): Boolean = transaction {
        val row = Settlements.selectAll()
            .where { Settlements.id eq settlementId }
            .singleOrNull() ?: return@transaction false

        if (row[Settlements.creditorId] != actorId) return@transaction false

        if (row[Settlements.status] != SettlementStatus.PAID) return@transaction false

        Settlements.update({ Settlements.id eq settlementId }) {
            it[status] = SettlementStatus.DISPUTED
            it[updatedAt] = System.currentTimeMillis()
        } > 0
    }
}