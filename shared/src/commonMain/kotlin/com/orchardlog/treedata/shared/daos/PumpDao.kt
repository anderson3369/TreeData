package com.orchardlog.treedata.shared.daos

import androidx.room.*
import com.orchardlog.treedata.shared.model.Pump
import kotlinx.coroutines.flow.Flow

@Dao
interface PumpDao {
    @Insert
    suspend fun insert(pump: Pump): Long

    @Update
    suspend fun update(pump: Pump)

    @Delete
    suspend fun delete(pump: Pump)

    @Query("SELECT * FROM Pump")
    fun getPumps(): Flow<List<Pump>>

    @Query("SELECT * FROM Pump")
    fun getPumpMap(): Flow<Map<@MapColumn(columnName = "id") Long, Pump>>
}
