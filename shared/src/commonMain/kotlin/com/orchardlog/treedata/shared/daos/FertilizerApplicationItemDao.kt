package com.orchardlog.treedata.shared.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.orchardlog.treedata.shared.model.FertilizerApplicationItem
import com.orchardlog.treedata.shared.model.WeightOrMeasureUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface FertilizerApplicationItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fertilizerApplicationItem: FertilizerApplicationItem)

    @Update
    suspend fun update(fertilizerApplicationItem: FertilizerApplicationItem)

    @Delete
    suspend fun delete(fertilizerApplicationItem: FertilizerApplicationItem)

    @Query("DELETE FROM FertilizerApplicationItem WHERE fertilizerApplicationId = :applicationId")
    suspend fun deleteByApplicationId(applicationId: Long)

    @Query("SELECT * FROM FertilizerApplicationItem")
    fun getFertilizerApplicationItems(): Flow<List<FertilizerApplicationItem>>

    @Query("""
        SELECT 
            fai.fertilizerApplicationId,
            fai.fertilizerId,
            f.name AS fertilizerName, 
            fai.applied AS applied, 
            fai.appliedUnit AS appliedUnit,
            fa.applicationStart AS applicationStart
        FROM FertilizerApplicationItem fai
        INNER JOIN Fertilizer f ON fai.fertilizerId = f.id
        INNER JOIN FertilizerApplication fa ON fai.fertilizerApplicationId = fa.id
        WHERE fa.applicationStart >= :startDate AND fa.applicationStart <= :endDate
    """)
    fun getFertilizerApplicationItemsByDate(startDate: Instant, endDate: Instant): Flow<List<FertilizerApplicationItemByDate>>

    @Query("""
        SELECT 
            fai.fertilizerApplicationId,
            fai.fertilizerId,
            f.name AS fertilizerName, 
            fai.applied AS applied, 
            fai.appliedUnit AS appliedUnit,
            fa.applicationStart AS applicationStart
        FROM FertilizerApplicationItem fai
        INNER JOIN Fertilizer f ON fai.fertilizerId = f.id
        INNER JOIN FertilizerApplication fa ON fai.fertilizerApplicationId = fa.id
        ORDER BY fa.applicationStart DESC
    """)
    fun getAllFertilizerApplicationItemsJoined(): Flow<List<FertilizerApplicationItemByDate>>
}

data class FertilizerApplicationItemByDate(
    val fertilizerApplicationId: Long,
    val fertilizerId: Long,
    val fertilizerName: String,
    val applied: Double,
    val appliedUnit: WeightOrMeasureUnit,
    val applicationStart: Instant
) {
    override fun toString(): String {
        return "$fertilizerName: $applied $appliedUnit"
    }
}
