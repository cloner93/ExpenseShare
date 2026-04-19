package usecase.settlement

import repository.SettlementRepository

class GetSettlementUseCase(private val settlementRepository: SettlementRepository) {
    suspend operator fun invoke(groupId: Int) =
        settlementRepository.getSettlement(groupId)
}