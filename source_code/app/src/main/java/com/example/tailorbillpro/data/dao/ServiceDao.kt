package com.example.tailorbillpro.data.dao

import androidx.room.*
import com.example.tailorbillpro.data.entity.ServiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services ORDER BY name ASC")
    fun getAllServices(): Flow<List<ServiceEntity>>

    @Insert
    suspend fun insertService(service: ServiceEntity): Long

    @Update
    suspend fun updateService(service: ServiceEntity)

    @Delete
    suspend fun deleteService(service: ServiceEntity)

    @Query("DELETE FROM services")
    suspend fun deleteAllServices()
}
