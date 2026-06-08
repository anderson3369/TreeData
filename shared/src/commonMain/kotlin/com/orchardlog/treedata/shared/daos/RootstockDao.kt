package com.orchardlog.treedata.shared.daos

import androidx.room.*
import com.orchardlog.treedata.shared.model.Rootstock
import kotlinx.coroutines.flow.Flow

@Dao
interface RootstockDao {
    @Insert
    suspend fun insert(rootstock: Rootstock): Long

    @Update
    suspend fun update(rootstock: Rootstock)

    @Delete
    suspend fun delete(rootstock: Rootstock)

    @Query("SELECT * FROM Rootstock")
    fun getRootstocks(): Flow<List<Rootstock>>

    @Query("SELECT id, name FROM Rootstock")
    fun getRootstocksMap(): Flow<Map<@MapColumn(columnName = "id") Long, @MapColumn(columnName = "name") String>>
}
