package org.milad.expense_share.domain.service


import org.milad.expense_share.Amount
import org.milad.expense_share.data.models.TransactionStatus
import org.milad.expense_share.data.models.User
import org.milad.expense_share.domain.model.SettlementTransaction
import org.milad.expense_share.domain.repository.GroupRepository
import org.milad.expense_share.domain.repository.TransactionRepository
import kotlin.math.min

class SettlementService(
    private val groupRepository: GroupRepository,
    private val transactionRepository: TransactionRepository,
) {

    private fun calculateNetBalances(
        totalPaidByUser: Map<User, Amount>,
        totalShareOfUser: Map<User, Amount>
    ): Map<User, Amount> {
        val balances = mutableMapOf<User, Amount>()
        val allUserIds = totalPaidByUser.keys + totalShareOfUser.keys

        for (user in allUserIds) {
            val paid = totalPaidByUser[user] ?: Amount(0)
            val share = totalShareOfUser[user] ?: Amount(0)
            balances[user] = paid - share
        }

        val totalBalance = Amount(balances.values.sumOf { it.value })
        if (!totalBalance.isZero()) {
            throw IllegalArgumentException("Balance sum is not zero: ${totalBalance.value}")
        }

        return balances
    }

    private fun calculateSettlements(
        balances: Map<User, Amount>
    ): List<SettlementTransaction> {
        val transactions = mutableListOf<SettlementTransaction>()

        val creditors = balances
            .filter { it.value.isPositive() }
            .map { Pair(it.key, it.value) }
            .sortedByDescending { it.second }
            .toMutableList()

        val debtors = balances
            .filter { it.value.isNegative() }
            .map { Pair(it.key, -it.value) }
            .sortedByDescending { it.second }
            .toMutableList()

        var i = 0
        var j = 0

        while (i < creditors.size && j < debtors.size) {
            val creditor = creditors[i]
            val debtor = debtors[j]

            val settleAmount = min(creditor.second.value, debtor.second.value)

            transactions.add(
                SettlementTransaction(
                    fromUserId = debtor.first.id,
                    toUserId = creditor.first.id,
                    amount = Amount(settleAmount)
                )
            )

            creditors[i] = creditor.copy(second = creditor.second - settleAmount)
            debtors[j] = debtor.copy(second = debtor.second - settleAmount)

            if (creditors[i].second.isZero()) i++
            if (debtors[j].second.isZero()) j++
        }

        return transactions
    }


    fun groupSettlement(
        groupId: Int,
        userId: Int,
    ): Result<List<SettlementTransaction>> {

        try{
            val group = groupRepository.getGroupsOfUser(userId)
                .firstOrNull { it.id == groupId }
                ?: throw IllegalArgumentException("گروه مورد نظر یافت نشد یا شما به آن دسترسی ندارید")

            val totalGroupPaid = mutableMapOf<User, Amount>()
            val totalGroupShare = mutableMapOf<User, Amount>()

            group.transactions.forEach { trx ->
                if (trx.status == TransactionStatus.APPROVED) {
                    trx.payers?.forEach { payer ->
                        val currentPaid = totalGroupPaid[payer.user] ?: Amount(0)
                        totalGroupPaid[payer.user] = currentPaid + payer.amountPaid
                    }

                    trx.shareDetails?.members?.forEach { item ->
                        val currentShare = totalGroupShare[item.user] ?: Amount(0)
                        totalGroupShare[item.user] = currentShare + item.share
                    }
                }
            }

            val groupUserNetBalance = calculateNetBalances(
                totalPaidByUser = totalGroupPaid,
                totalShareOfUser = totalGroupShare
            )

            val res = calculateSettlements(groupUserNetBalance)
            return Result.success(res)
        }catch (e: Exception){

            return Result.failure(e)
        }
    }

}