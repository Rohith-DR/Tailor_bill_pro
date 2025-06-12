package com.example.tailorbillpro.data.entity

import androidx.room.*

@Entity(
    tableName = "bill_items",
    foreignKeys = [
        ForeignKey(
            entity = BillEntity::class,
            parentColumns = ["id"],
            childColumns = ["billId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("billId")]
)
data class BillItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val billId: Long,
    val serviceName: String,
    val quantity: Int,
    val price: Double
)
