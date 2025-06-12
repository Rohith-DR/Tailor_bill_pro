package com.example.tailorbillpro.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created ServiceEntity for storing predefined services
 */
@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val price: Double
)
