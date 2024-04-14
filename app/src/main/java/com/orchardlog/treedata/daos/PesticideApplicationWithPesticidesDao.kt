package com.orchardlog.treedata.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RoomWarnings
import androidx.room.Transaction
import com.orchardlog.treedata.entities.PesticideApplicationWithPesticides
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface PesticideApplicationWithPesticidesDao {

    @Transaction
    @Query("SELECT * FROM PesticideApplication PA " +
            "JOIN Pesticide P ON PA.pesticideId = P.id " +
            "WHERE orchardId = :orchardId AND PA.applicationStart BETWEEN :startDate AND :endDate")
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    fun getPesticideApplicationWithPesticides(orchardId: Long, startDate: LocalDate, endDate: LocalDate):
            Flow<MutableList<PesticideApplicationWithPesticides>>
}