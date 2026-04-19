package repository

import NetworkManager
import kotlinx.coroutines.flow.Flow
import model.Settlement

class SettlementRepositoryImpl(private val networkManager: NetworkManager): SettlementRepository {
    override suspend fun getSettlement(groupId: Int): Flow<Result<List<Settlement>>> {
        return networkManager.get<List<Settlement>>("/groups/$groupId/settlement")
    }

    override suspend fun paySettlement(
        groupId: Int,
        settlement: Int
    ): Flow<Result<String>> {
        return networkManager.post<Unit, String>("/groups/$groupId/settlement/$settlement/pay")
    }

    override suspend fun confirmSettlement(
        groupId: Int,
        settlement: Int
    ): Flow<Result<String>> {
        return networkManager.post<Unit, String>("/groups/$groupId/settlement/$settlement/confirm")
    }

    override suspend fun disputeSettlement(
        groupId: Int,
        settlement: Int
    ): Flow<Result<String>> {
        return networkManager.post<Unit, String>("/groups/$groupId/settlement/$settlement/dispute")
    }
}