package repository

import NetworkManager
import kotlinx.coroutines.flow.Flow
import model.SettlementTransaction

class SettlementRepositoryImpl(private val networkManager: NetworkManager): SettlementRepository {
    override suspend fun getGroupSettlement(groupId: Int): Flow<Result<List<SettlementTransaction>>> {
        return networkManager.get<List<SettlementTransaction>>("/groups/$groupId/settlement")
    }
}