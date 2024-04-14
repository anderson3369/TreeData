package com.orchardlog.treedata.daos

import androidx.room.*
import com.orchardlog.treedata.entities.Rootstock
import kotlinx.coroutines.flow.Flow


@Dao
interface RootstockDao {
    @Insert
    suspend fun insert(rootstock: Rootstock): Long

    @Update
    fun update(rootstock: Rootstock)

    @Delete
    fun delete(rootstock: Rootstock)

    @Query("SELECT * FROM Rootstock")
    fun getRootstocks(): Flow<MutableList<Rootstock>>

    @Query("SELECT id, name FROM Rootstock")
    @MapInfo(keyColumn = "id", valueColumn = "name")
    fun getRootstocksMap(): Flow<Map<Long, String>>
}