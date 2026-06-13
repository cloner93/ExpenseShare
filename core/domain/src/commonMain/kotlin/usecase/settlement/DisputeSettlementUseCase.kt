package usecase.settlement

import repository.SettlementRepository

class DisputeSettlementUseCase(private val settlementRepository: SettlementRepository) {
    suspend operator fun invoke(groupId: Int, settlementId: Int) =
        settlementRepository.disputeSettlement(groupId, settlementId)
}