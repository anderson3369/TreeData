package com.orchardmanager.treedata.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardmanager.treedata.entities.Farm
import com.orchardmanager.treedata.entities.FarmWithOrchards
import com.orchardmanager.treedata.entities.Orchard
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmWithOrchardsDao {
    @Transaction
    @Query("SELECT * FROM Farm")
    fun getFarmWithOrchards(): Flow<MutableList<FarmWithOrchards>>

}