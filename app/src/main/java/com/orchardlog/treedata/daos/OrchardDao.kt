package com.orchardlog.treedata.daos

import androidx.room.*
import com.orchardlog.treedata.entities.Orchard
import kotlinx.coroutines.flow.Flow

@Dao
interface OrchardDao {
    @Insert
    suspend fun insert(orchard: Orchard): Long

    @Update
    fun update(orchard: Orchard)

    @Delete
    fun delete(orchard: Orchard)

    @Query("SELECT * FROM Orchard WHERE farmId = :farmId")
    fun getOrchards(farmId: Long): Flow<MutableList<Orchard>>

    @Query("SELECT * FROM Orchard")
    fun getAllrchards(): Flow<MutableList<Orchard>>
}