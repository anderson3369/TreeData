package com.orchardmanager.treedata.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardmanager.treedata.entities.OrchardAndSpacing

@Dao
interface OrchardAndSpacingDao {
    @Transaction
    @Query("SELECT * FROM Orchard")
    fun getOrchardAndSpacing(): OrchardAndSpacing

    @Transaction
    @Query("SELECT * FROM Orchard WHERE id = :id")
    fun getOrchardAndSpacing(id: Long): OrchardAndSpacing


}