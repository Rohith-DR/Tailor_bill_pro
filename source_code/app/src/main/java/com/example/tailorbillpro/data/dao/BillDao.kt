package com.example.tailorbillpro.data.dao

import androidx.room.*
import com.example.tailorbillpro.data.entity.BillEntity
import com.example.tailorbillpro.data.entity.BillItemEntity
import com.example.tailorbillpro.data.entity.ClientEntity
import kotlinx.coroutines.flow.Flow

data class BillWithDetails(
    @Embedded val bill: BillEntity,
    @Relation(
        parentColumn = "clientId",
        entityColumn = "id"
    )
    val client: ClientEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "billId"
    )
    val items: List<BillItemEntity>
)

@Dao
interface BillDao {
    @Transaction
    @Query("SELECT * FROM bills ORDER BY date DESC")
    fun getAllBillsWithDetails(): Flow<List<BillWithDetails>>

    @Insert
    suspend fun insertBill(bill: BillEntity): Long

    @Update
    suspend fun updateBill(bill: BillEntity)

    @Query("DELETE FROM bills WHERE id = :billId")
    suspend fun deleteBill(billId: Long)

    @Query("SELECT * FROM bills WHERE id = :billId")
    suspend fun getBillById(billId: Long): BillEntity?
}
