package com.orchardlog.treedata.daos

import androidx.room.*
import com.orchardlog.treedata.entities.Variety
import kotlinx.coroutines.flow.Flow

@Dao
interface VarietyDao {
    @Insert
    suspend fun insert(variety: Variety): Long

    @Update
    fun update(variety: Variety)

    @Delete
    fun delete(variety: Variety)

    @Query("SELECT * FROM Variety")
    fun getVarieties(): Flow<MutableList<Variety>>

    @Query("SELECT id, name FROM Variety")
    //@MapInfo(keyColumn = "id", valueColumn = "name")
    fun getVarietiesMap(): Flow<Map<@MapColumn(columnName = "id") Long, @MapColumn(columnName = "name") String>>
}