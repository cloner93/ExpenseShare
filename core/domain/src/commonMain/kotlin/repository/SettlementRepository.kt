package repository

import io.mockative.Mockable
import kotlinx.coroutines.flow.Flow
import model.SettlementTransaction

@Mockable
interface SettlementRepository {
    suspend fun getGroupSettlement(groupId: Int): Flow<Result<List<SettlementTransaction>>>
}