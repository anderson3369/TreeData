package com.orchardlog.treedata.shared.daos

import androidx.room.*
import com.orchardlog.treedata.shared.model.Orchard
import com.orchardlog.treedata.shared.model.OrchardWithFarm
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface OrchardDao {
    @Insert
    suspend fun insert(orchard: Orchard): Long

    @Update
    suspend fun update(orchard: Orchard)

    @Delete
    suspend fun delete(orchard: Orchard)

    @Query("SELECT * FROM Orchard WHERE farmId = :farmId AND (validTo IS NULL OR validTo > :now)")
    fun getOrchardsByFarm(farmId: Long, now: Instant): Flow<List<Orchard>>

    @Query("SELECT * FROM Orchard WHERE validTo IS NULL OR validTo > :now")
    fun getOrchards(now: Instant): Flow<List<Orchard>>

    @Transaction
    @Query("SELECT * FROM Orchard WHERE validTo IS NULL OR validTo > :now")
    fun getOrchardsWithFarm(now: Instant): Flow<List<OrchardWithFarm>>

    @Query("SELECT * FROM Orchard WHERE persistentId = :persistentId AND (validFrom <= :asOf AND (validTo IS NULL OR validTo > :asOf))")
    suspend fun getOrchardByPersistentId(persistentId: String, asOf: Instant): Orchard?
}
