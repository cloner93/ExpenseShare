package repository

import io.mockative.Mockable
import kotlinx.coroutines.flow.Flow
import model.Settlement

@Mockable
interface SettlementRepository {
    suspend fun getSettlement(groupId: Int): Flow<Result<List<Settlement>>>
    suspend fun paySettlement(groupId: Int, settlement: Int): Flow<Result<String>>
    suspend fun confirmSettlement(groupId: Int, settlement: Int): Flow<Result<String>>
    suspend fun disputeSettlement(groupId: Int, settlement: Int): Flow<Result<String>>
}