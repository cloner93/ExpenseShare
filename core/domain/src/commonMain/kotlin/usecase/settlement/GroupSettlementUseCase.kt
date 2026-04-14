package usecase.settlement

import repository.SettlementRepository

class GroupSettlementUseCase(private val settlementRepository: SettlementRepository) {
    suspend operator fun invoke(groupId: Int) =
        settlementRepository.getGroupSettlement(groupId)
}