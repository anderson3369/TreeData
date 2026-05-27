package com.orchardlog.treedata.shared.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardlog.treedata.shared.model.FertilizerApplicationWithFertilizers
import kotlinx.datetime.Instant
import kotlinx.coroutines.flow.Flow

@Dao
interface FertilizerApplicationWithFertilizersDao {
    @Transaction
    @Query("SELECT * FROM FertilizerApplication " +
            "WHERE orchardId = :orchardId AND applicationStart BETWEEN :startDate AND :endDate")
    fun getFertilizerApplicationWithFertilizers(orchardId: Long, startDate: Instant, endDate: Instant):
            Flow<List<FertilizerApplicationWithFertilizers>>
}
