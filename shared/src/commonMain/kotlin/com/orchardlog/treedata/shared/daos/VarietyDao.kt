package com.orchardlog.treedata.shared.daos

import androidx.room.*
import com.orchardlog.treedata.shared.model.Variety
import kotlinx.coroutines.flow.Flow

@Dao
interface VarietyDao {
    @Insert
    suspend fun insert(variety: Variety): Long

    @Update
    suspend fun update(variety: Variety)

    @Delete
    suspend fun delete(variety: Variety)

    @Query("SELECT * FROM Variety")
    fun getVarieties(): Flow<List<Variety>>

    @Query("SELECT id, name FROM Variety")
    fun getVarietiesMap(): Flow<Map<@MapColumn(columnName = "id") Long, @MapColumn(columnName = "name") String>>
}
