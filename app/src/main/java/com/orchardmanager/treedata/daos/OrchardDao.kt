package com.orchardmanager.treedata.daos

import androidx.room.*
import com.orchardmanager.treedata.entities.Orchard
import kotlinx.coroutines.flow.Flow

@Dao
interface OrchardDao {
    @Insert
    fun insert(orchard: Orchard):Long

    @Update
    fun update(orchard: Orchard)

    @Delete
    fun delete(orchard: Orchard)

    @Query("SELECT * FROM Orchard WHERE farmId = :farmId")
    fun getOrchards(farmId: Long): Flow<MutableList<Orchard>>

    @Query("SELECT * FROM Orchard")
    fun getAllrchards(): Flow<MutableList<Orchard>>
}