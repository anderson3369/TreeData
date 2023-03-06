package com.orchardmanager.treedata.daos

import androidx.room.Dao
import androidx.room.MapInfo
import androidx.room.Query
import androidx.room.Transaction
import com.orchardmanager.treedata.entities.Farm
import com.orchardmanager.treedata.entities.FarmWithOrchards
import com.orchardmanager.treedata.entities.Orchard
import kotlinx.coroutines.flow.Flow
import java.util.*
import kotlin.collections.LinkedHashMap

@Dao
interface FarmWithOrchardsDao {
    @Transaction
    @Query("SELECT * FROM Farm")
    fun getFarmWithOrchards(): Flow<MutableList<FarmWithOrchards>>

    @Transaction
    @MapInfo(keyColumn = "orchardId", valueColumn = "farmSite")
    @Query("SELECT Orchard.id AS orchardId, Farm.name || ' - ' || Orchard.crop AS farmSite FROM Farm JOIN Orchard ON Farm.id = Orchard.farmId")
    fun getFarmWithOrchardsMap(): Flow<Map<Long, String>>

}