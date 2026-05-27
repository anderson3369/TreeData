package com.orchardlog.treedata.shared.daos

import androidx.room.*
import com.orchardlog.treedata.shared.model.Farm
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface FarmDao {
    @Insert
    suspend fun insert(farm: Farm): Long

    @Update
    suspend fun update(farm: Farm)

    @Delete
    suspend fun delete(farm: Farm)

    @Query("SELECT * FROM Farm WHERE farmerId = :farmerId AND (validTo IS NULL OR validTo > :now)")
    fun getFarmById(farmerId: Long, now: Instant): Flow<List<Farm>>

    @Query("SELECT * FROM Farm WHERE validTo IS NULL OR validTo > :now")
    fun getFarms(now: Instant): Flow<List<Farm>>

    @Query("SELECT * FROM Farm WHERE persistentId = :persistentId AND (validFrom <= :asOf AND (validTo IS NULL OR validTo > :asOf))")
    suspend fun getFarmByPersistentId(persistentId: String, asOf: Instant): Farm?
}
