package com.orchardlog.treedata.shared.daos

import androidx.room.*
import com.orchardlog.treedata.shared.model.Irrigation
import kotlinx.datetime.Instant
import kotlinx.coroutines.flow.Flow

@Dao
interface IrrigationDao {

    @Insert
    suspend fun insert(irrigation: Irrigation): Long

    @Update
    suspend fun update(irrigation: Irrigation)

    @Delete
    suspend fun delete(irrigation: Irrigation)

    @Query("SELECT * FROM Irrigation")
    fun getIrrigations(): Flow<List<Irrigation>>

    /**
     * Return the irrigations for a season
     */
    @Query("SELECT * FROM Irrigation WHERE startTime BETWEEN :firstYear AND :endYear ORDER BY startTime DESC")
    fun getIrrigationsBySeason(firstYear: Instant, endYear: Instant): Flow<List<Irrigation>>

    @Query("SELECT * FROM Irrigation WHERE firestoreId = :firestoreId LIMIT 1")
    suspend fun getByFirestoreId(firestoreId: String): Irrigation?

    @Query("DELETE FROM Irrigation WHERE firestoreId = :firestoreId")
    suspend fun deleteByFirestoreId(firestoreId: String)
}
