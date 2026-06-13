package usecase.settlement

import repository.SettlementRepository

class ConfirmSettlementUseCase(private val settlementRepository: SettlementRepository) {
    suspend operator fun invoke(groupId: Int, settlementId: Int) =
        settlementRepository.confirmSettlement(groupId, settlementId)
}