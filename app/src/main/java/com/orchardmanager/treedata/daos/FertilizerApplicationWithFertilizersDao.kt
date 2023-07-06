package com.orchardmanager.treedata.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RoomWarnings
import androidx.room.Transaction
import com.orchardmanager.treedata.entities.FertilizerApplication
import com.orchardmanager.treedata.entities.FertilizerApplicationWithFertilizers
import com.orchardmanager.treedata.entities.PesticideApplicationWithPesticides
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface FertilizerApplicationWithFertilizersDao {
    @Transaction
    @Query("SELECT * FROM FertilizerApplication FA " +
            "JOIN Fertilizer F ON FA.fertilizerId = F.id " +
            "WHERE orchardId = :orchardId AND FA.applicationStart BETWEEN :startDate AND :endDate")
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    fun getFertilizerApplicationWithFertilizers(orchardId: Long, startDate: LocalDate, endDate: LocalDate):
            Flow<MutableList<FertilizerApplicationWithFertilizers>>
}