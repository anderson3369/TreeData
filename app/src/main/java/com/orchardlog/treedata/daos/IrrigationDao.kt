package com.orchardlog.treedata.daos

import androidx.room.*
import com.orchardlog.treedata.data.DateConverter
import com.orchardlog.treedata.entities.Irrigation
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface IrrigationDao {

    @Insert
    suspend fun insert(irrigation: Irrigation): Long

    @Update
    fun update(irrigation: Irrigation)

    @Delete
    fun delete(irrigation: Irrigation)

    @Query("SELECT * FROM Irrigation")
    fun getIrrigations(): Flow<MutableList<Irrigation>>

    /**
     * Return the irrigations for a season
     */
    @TypeConverters(DateConverter::class)
    @Query("SELECT * FROM Irrigation WHERE startTime BETWEEN :firstYear AND :endYear ORDER BY startTime DESC")
    fun getIrrigations(firstYear: LocalDate, endYear: LocalDate): Flow<MutableList<Irrigation>>


}