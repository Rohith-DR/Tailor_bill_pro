package com.example.tailorbillpro.data.dao

import androidx.room.*
import com.example.tailorbillpro.data.entity.BillItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BillItemDao {
    @Query("SELECT * FROM bill_items WHERE billId = :billId")
    fun getBillItems(billId: Long): Flow<List<BillItemEntity>>

    @Insert
    suspend fun insertBillItem(billItem: BillItemEntity): Long

    @Update
    suspend fun updateBillItem(billItem: BillItemEntity)

    @Delete
    suspend fun deleteBillItem(billItem: BillItemEntity)

    @Query("DELETE FROM bill_items WHERE billId = :billId")
    suspend fun deleteBillItems(billId: Long)
}
