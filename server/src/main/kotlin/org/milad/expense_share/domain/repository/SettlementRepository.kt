package org.milad.expense_share.domain.repository

import org.milad.expense_share.data.models.Settlement

interface SettlementRepository {
    fun getByGroup(groupId: Int): List<Settlement>
    fun replaceAll(groupId: Int, settlements: List<Settlement>)
    fun markAsPaid(settlementId: Int, actorId: Int): Boolean
    fun confirm(settlementId: Int, actorId: Int): Boolean
    fun dispute(settlementId: Int, actorId: Int): Boolean
}