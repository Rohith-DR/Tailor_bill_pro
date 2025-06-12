package com.example.tailorbillpro.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.tailorbillpro.data.dao.*
import com.example.tailorbillpro.data.entity.*
import com.example.tailorbillpro.data.utils.Converters

@Database(
    entities = [
        ServiceEntity::class,
        ClientEntity::class,
        BillEntity::class,
        BillItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceDao(): ServiceDao
    abstract fun clientDao(): ClientDao
    abstract fun billDao(): BillDao
    abstract fun billItemDao(): BillItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tailor_bill_pro_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
