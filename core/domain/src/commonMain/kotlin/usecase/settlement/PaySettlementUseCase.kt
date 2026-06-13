package usecase.settlement

import repository.SettlementRepository

class PaySettlementUseCase(private val settlementRepository: SettlementRepository) {
    suspend operator fun invoke(groupId: Int, settlementId: Int) =
        settlementRepository.paySettlement(groupId, settlementId)
}