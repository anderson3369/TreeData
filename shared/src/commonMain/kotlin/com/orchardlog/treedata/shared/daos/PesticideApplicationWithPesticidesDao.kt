package com.orchardlog.treedata.shared.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardlog.treedata.shared.model.PesticideApplicationWithPesticides
import kotlinx.datetime.Instant
import kotlinx.coroutines.flow.Flow

@Dao
interface PesticideApplicationWithPesticidesDao {

    @Transaction
    @Query("SELECT * FROM PesticideApplication WHERE orchardId = :orchardId AND applicationStart BETWEEN :startDate AND :endDate")
    fun getPesticideApplicationWithPesticides(orchardId: Long, startDate: Instant, endDate: Instant):
            Flow<List<PesticideApplicationWithPesticides>>
}
