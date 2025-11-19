package org.milad.expense_share.data.repository

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.milad.expense_share.data.db.table.Groups
import org.milad.expense_share.data.db.table.TransactionPayers
import org.milad.expense_share.data.db.table.TransactionShareMembers
import org.milad.expense_share.data.db.table.TransactionShares
import org.milad.expense_share.data.db.table.Transactions
import org.milad.expense_share.data.models.Transaction
import org.milad.expense_share.data.models.TransactionStatus
import org.milad.expense_share.domain.repository.TransactionRepository
import org.milad.expense_share.presentation.transactions.model.PayerRequest
import org.milad.expense_share.presentation.transactions.model.ShareDetailsRequest
import org.milad.expense_share.presentation.transactions.model.ShareMemberRequest
import org.milad.expense_share.presentation.transactions.model.ShareType
import kotlin.math.abs

class TransactionRepositoryImpl : TransactionRepository {

    override fun createTransaction(
        groupId: Int,
        userId: Int,
        title: String,
        amount: Double,
        description: String,
        payers: List<PayerRequest>,
        shareDetails: ShareDetailsRequest,
    ): Transaction? = transaction {
        val groupRow = Groups.selectAll().where { Groups.id eq groupId }.singleOrNull()
            ?: return@transaction null
        val ownerId = groupRow[Groups.ownerId]

        val status =
            if (ownerId == userId) TransactionStatus.APPROVED else TransactionStatus.PENDING
        val approvedBy = if (status == TransactionStatus.APPROVED) userId else null

        if (payers.isEmpty()) throw IllegalArgumentException("payers must not be empty")

        val sumPayers = payers.sumOf { it.amountPaid }
        val eps = 0.0001
        if (abs(sumPayers - amount) > eps) {
            throw IllegalArgumentException("sum of payers (${sumPayers}) does not equal total amount ($amount)")
        }

        val memberIdsFromReq = shareDetails.members.map { it.userId }.distinct()

        val finalMemberIds =
            memberIdsFromReq.ifEmpty { throw IllegalArgumentException("members required for ${shareDetails.type} split") }

        val txnId = Transactions.insert {
            it[Transactions.groupId] = groupId
            it[Transactions.title] = title
            it[Transactions.amount] = amount
            it[Transactions.description] = description
            it[Transactions.createdBy] = userId
            it[Transactions.status] = status
            it[Transactions.approvedBy] = approvedBy
            it[Transactions.createdAt] = System.currentTimeMillis()
            it[Transactions.transactionDate] = System.currentTimeMillis()
        } get Transactions.id

        TransactionPayers.batchInsert(payers) { payer ->
            this[TransactionPayers.transactionId] = txnId
            this[TransactionPayers.userId] = payer.userId
            this[TransactionPayers.amountPaid] = payer.amountPaid
        }

        val shareId = TransactionShares.insert {
            it[TransactionShares.transactionId] = txnId
            it[TransactionShares.type] = shareDetails.type.name
        } get TransactionShares.id

        val shareMembersToInsert: List<Pair<Int, Double>> = when (shareDetails.type) {
            ShareType.Equal -> {
                val n = finalMemberIds.size
                val per = amount / n
                finalMemberIds.map { it to per }
            }

            ShareType.Percent -> {
                val members = shareDetails.members
                val sumPercent = members.sumOf { it.share }
                if (abs(sumPercent - 100.0) > 1e-6) {
                    throw IllegalArgumentException("percent shares must sum to 100, sum=$sumPercent")
                }
                members.map { it.userId to (amount * it.share / 100.0) }
            }

            ShareType.Weight -> {
                val members = shareDetails.members
                val totalWeight = members.sumOf { it.share }
                if (totalWeight <= 0.0) throw IllegalArgumentException("total weight must be > 0")
                members.map { it.userId to (amount * (it.share / totalWeight)) }
            }

            ShareType.Manual -> {
                val members = shareDetails.members
                val sumShares = members.sumOf { it.share }
                if (abs(sumShares - amount) > 1e-6) {
                    throw IllegalArgumentException("manual shares must sum to amount; sum=$sumShares amount=$amount")
                }
                members.map { it.userId to it.share }
            }
        }

        TransactionShareMembers.batchInsert(shareMembersToInsert) { pair ->
            val (uid, shareVal) = pair
            this[TransactionShareMembers.shareId] = shareId
            this[TransactionShareMembers.userId] = uid
            this[TransactionShareMembers.share] = shareVal
        }

        Transaction(
            id = txnId,
            groupId = groupId,
            title = title,
            amount = amount,
            description = description,
            createdBy = userId,
            status = status,
            createdAt = System.currentTimeMillis(),
            transactionDate = System.currentTimeMillis(),
            approvedBy = approvedBy,
            payers = payers,
            shareDetails = shareDetails,
        )
    }

//    override fun getTransactions(userId: Int, groupId: Int): List<Transaction> = transaction {
//        val group = Groups
//            .selectAll().where { Groups.id eq groupId }
//            .singleOrNull() ?: return@transaction emptyList()
//
//        val ownerId = group[Groups.ownerId]
//
//        val baseQuery = if (ownerId == userId) {
//            Transactions.selectAll().where { Transactions.groupId eq groupId }
//        } else {
//            Transactions.selectAll().where {
//                (Transactions.groupId eq groupId) and
//                        (
//                                (Transactions.status eq TransactionStatus.APPROVED) or
//                                        ((Transactions.status eq TransactionStatus.PENDING) and (Transactions.createdBy eq userId))
//                                )
//            }
//        }
//
//        val txList = baseQuery.map { it.toTransaction() }
//
//        txList.map { tx ->
//
//            val payers = TransactionPayers
//                .selectAll().where { TransactionPayers.transactionId eq tx.id }
//                .map {
//                    PayerRequest(
//                        userId = it[TransactionPayers.userId],
//                        amountPaid = it[TransactionPayers.amountPaid]
//                    )
//                }
//
//            val split = TransactionShares
//                .selectAll().where { TransactionShares.transactionId eq tx.id }
//                .singleOrNull()
//
//            val shareDetails =
//                if (split == null) null
//                else {
//                    val members = TransactionShareMembers
//                        .selectAll().where { TransactionShareMembers.shareId eq tx.id }
//                        .map {
//                            ShareMemberRequest(
//                                userId = it[TransactionShareMembers.userId],
//                                share = it[TransactionShareMembers.share]
//                            )
//                        }
//
//                    ShareDetailsRequest(
//                        type = split[TransactionShares.type],
//                        members = members
//                    )
//                }
//
//            tx.copy(
//                payers = payers,
//                shareDetails = shareDetails
//            )
//        }
//    }

    override fun getTransactions(userId: Int, groupId: Int): List<Transaction> = transaction {
        val group = Groups
            .selectAll()
            .where { Groups.id eq groupId }
            .singleOrNull() ?: return@transaction emptyList()

        val ownerId = group[Groups.ownerId]

        // --- تعیین سطح دسترسی کاربر ---
        val baseQuery = if (ownerId == userId) {
            Transactions
                .selectAll()
                .where { Transactions.groupId eq groupId }
        } else {
            Transactions
                .selectAll()
                .where {
                    (Transactions.groupId eq groupId) and
                            (
                                    (Transactions.status eq TransactionStatus.APPROVED) or
                                            ((Transactions.status eq TransactionStatus.PENDING) and
                                                    (Transactions.createdBy eq userId))
                                    )
                }
        }

        // --- ساختن مدل کامل Transaction ---
        baseQuery.map { row ->
            val transactionId = row[Transactions.id]

            // ----- payers -----
            val payers = TransactionPayers
                .selectAll().where { TransactionPayers.transactionId eq transactionId }
                .map {
                    PayerRequest(
                        userId = it[TransactionPayers.userId],
                        amountPaid = it[TransactionPayers.amountPaid]
                    )
                }

            // ----- Share Type -----
            val shareMain = TransactionShares
                .selectAll().where { TransactionShares.transactionId eq transactionId }
                .singleOrNull()


            val shareDetails = if (shareMain != null) {

                // members
                val members = TransactionShareMembers
                    .selectAll()
                    .where { TransactionShareMembers.shareId eq shareMain[TransactionShares.id] }
                    .map {
                        ShareMemberRequest(
                            userId = it[TransactionShareMembers.userId],
                            share = it[TransactionShareMembers.share]
                        )
                    }
                val typeString = shareMain[TransactionShares.type]

                ShareDetailsRequest(
                    type = ShareType.valueOf(typeString),
                    members = members
                )
            } else {
                // هیچ داده‌ای ثبت نشده، ولی مدل باید برگرده
                ShareDetailsRequest(
                    type = ShareType.Equal,
                    members = emptyList()
                )
            }

            Transaction(
                id = transactionId,
                groupId = row[Transactions.groupId],
                title = row[Transactions.title],
                amount = row[Transactions.amount],
                description = row[Transactions.description],
                createdBy = row[Transactions.createdBy],
                status = row[Transactions.status],
                createdAt = row[Transactions.createdAt],
                transactionDate = row[Transactions.transactionDate],
                approvedBy = row[Transactions.approvedBy],
                payers = payers,
                shareDetails = shareDetails
            )
        }
    }


    override fun approveTransaction(transactionId: Int, managerId: Int): Boolean = transaction {
        val tx = Transactions.selectAll().where { Transactions.id eq transactionId }.singleOrNull()
            ?: return@transaction false
        val group =
            Groups.selectAll().where { Groups.id eq tx[Transactions.groupId] }.singleOrNull()
                ?: return@transaction false

        if (group[Groups.ownerId] != managerId) return@transaction false

        Transactions.update({ Transactions.id eq transactionId }) {
            it[status] = TransactionStatus.APPROVED
            it[approvedBy] = managerId
        }
        true
    }

    override fun rejectTransaction(transactionId: Int, managerId: Int): Boolean = transaction {
        val tx = Transactions.selectAll().where { Transactions.id eq transactionId }.singleOrNull()
            ?: return@transaction false
        val group =
            Groups.selectAll().where { Groups.id eq tx[Transactions.groupId] }.singleOrNull()
                ?: return@transaction false

        if (group[Groups.ownerId] != managerId) return@transaction false

        Transactions.update({ Transactions.id eq transactionId }) {
            it[status] = TransactionStatus.REJECTED
            it[approvedBy] = managerId
        }
        true
    }

    override fun deleteTransaction(transactionId: Int, managerId: Int): Boolean = transaction {
        val tx = Transactions.selectAll().where { Transactions.id eq transactionId }.singleOrNull()
            ?: return@transaction false
        val group =
            Groups.selectAll().where { Groups.id eq tx[Transactions.groupId] }.singleOrNull()
                ?: return@transaction false

        if (group[Groups.ownerId] != managerId) return@transaction false
        Transactions.deleteWhere { Transactions.id eq transactionId } > 0
    }
}
