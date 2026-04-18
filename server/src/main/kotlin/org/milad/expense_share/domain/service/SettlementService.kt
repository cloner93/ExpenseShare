package org.milad.expense_share.domain.service

import org.milad.expense_share.Amount
import org.milad.expense_share.data.models.Settlement
import org.milad.expense_share.data.models.TransactionStatus
import org.milad.expense_share.data.models.User
import org.milad.expense_share.domain.model.SettlementStatus
import org.milad.expense_share.domain.repository.GroupRepository
import org.milad.expense_share.domain.repository.SettlementRepository
import org.milad.expense_share.domain.repository.TransactionRepository
import org.milad.expense_share.domain.repository.UserRepository
import kotlin.math.min

class SettlementService(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
    private val transactionRepository: TransactionRepository,
    private val settlementRepository: SettlementRepository,
) {

    fun recalculate(groupId: Int, requesterId: Int) {
        val transactions = transactionRepository.getTransactions(requesterId, groupId)
        val approved = transactions.filter { it.status == TransactionStatus.APPROVED }

        if (approved.isEmpty()) {
            settlementRepository.replaceAll(groupId, emptyList())
            return
        }

        val balances = buildNetBalances(approved)
        val settlements = buildSettlements(groupId, balances)
        settlementRepository.replaceAll(groupId, settlements)
    }

    fun getGroupSettlements(groupId: Int, userId: Int): Result<List<Settlement>> {
        return try {
            val isMember = groupRepository.getGroupsOfUser(userId)
                .any { it.id == groupId }
            if (!isMember) return Result.failure(
                IllegalAccessException("Access denied: user is not a member of this group")
            )
            Result.success(settlementRepository.getByGroup(groupId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun markAsPaid(settlementId: Int, userId: Int): Result<String> {
        val updated = settlementRepository.markAsPaid(settlementId, userId)
        return if (updated) Result.success("Payment marked. Waiting for creditor confirmation.")
        else Result.failure(IllegalAccessException("Only the debtor can mark this as paid, or status is not PENDING."))
    }

    fun confirmPayment(settlementId: Int, userId: Int): Result<String> {
        val updated = settlementRepository.confirm(settlementId, userId)
        return if (updated) Result.success("Payment confirmed. Settlement is complete.")
        else Result.failure(IllegalAccessException("Only the creditor can confirm, or status is not PAID."))
    }

    fun disputePayment(settlementId: Int, userId: Int): Result<String> {
        val updated = settlementRepository.dispute(settlementId, userId)
        return if (updated) Result.success("Payment disputed. Settlement is back to PENDING.")
        else Result.failure(IllegalAccessException("Only the creditor can dispute, or status is not PAID."))
    }

    private fun buildNetBalances(
        transactions: List<org.milad.expense_share.data.models.Transaction>
    ): Map<User, Amount> {
        val paid = mutableMapOf<User, Amount>()
        val share = mutableMapOf<User, Amount>()

        transactions.forEach { trx ->
            trx.payers?.forEach { payer ->
                paid[payer.user] = (paid[payer.user] ?: Amount(0)) + payer.amountPaid
            }
            trx.shareDetails?.members?.forEach { member ->
                share[member.user] = (share[member.user] ?: Amount(0)) + member.share
            }
        }

        val allUsers = paid.keys + share.keys
        val balances = mutableMapOf<User, Amount>()
        for (user in allUsers) {
            balances[user] = (paid[user] ?: Amount(0)) - (share[user] ?: Amount(0))
        }

        val total = Amount(balances.values.sumOf { it.value })
        if (!total.isZero()) {
            throw IllegalStateException(
                "Net balance is not zero (${total.value}). Data may be inconsistent."
            )
        }

        return balances
    }

    private fun buildSettlements(
        groupId: Int,
        balances: Map<User, Amount>
    ): List<Settlement> {
        val now = System.currentTimeMillis()
        val result = mutableListOf<Settlement>()

        val creditors = balances
            .filter { it.value.isPositive() }
            .map { it.key to it.value }
            .sortedByDescending { it.second }
            .toMutableList()

        val debtors = balances
            .filter { it.value.isNegative() }
            .map { it.key to -it.value }
            .sortedByDescending { it.second }
            .toMutableList()

        var i = 0
        var j = 0

        while (i < creditors.size && j < debtors.size) {
            val (creditor, creditAmt) = creditors[i]
            val (debtor, debtAmt) = debtors[j]

            val settleAmt = min(creditAmt.value, debtAmt.value)

            result.add(
                Settlement(
                    id = 0,
                    groupId = groupId,
                    debtor = debtor,
                    creditor = creditor,
                    amount = Amount(settleAmt),
                    status = SettlementStatus.PENDING,
                    createdAt = now,
                    updatedAt = now,
                )
            )

            creditors[i] = creditor to (creditAmt - settleAmt)
            debtors[j] = debtor to (debtAmt - settleAmt)

            if (creditors[i].second.isZero()) i++
            if (debtors[j].second.isZero()) j++
        }

        return result
    }
}