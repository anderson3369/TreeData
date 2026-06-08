package com.orchardlog.treedata.shared.daos

import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Transaction
import com.orchardlog.treedata.shared.model.FarmWithOrchards
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmWithOrchardsDao {
    @Transaction
    @Query("SELECT * FROM Farm")
    fun getFarmWithOrchards(): Flow<List<FarmWithOrchards>>

    @Transaction
    @Query("SELECT Orchard.id AS orchardId, Farm.name || ' - ' || Orchard.crop AS farmSite FROM Farm" +
            " JOIN Orchard ON Farm.id = Orchard.farmId")
    fun getFarmWithOrchardsMap(): Flow<Map<@MapColumn(columnName = "orchardId") Long, @MapColumn(columnName = "farmSite") String>>
}
