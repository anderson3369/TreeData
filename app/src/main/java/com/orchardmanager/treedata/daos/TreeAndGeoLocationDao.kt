package com.orchardmanager.treedata.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardmanager.treedata.entities.TreeAndGeoLocation

@Dao
interface TreeAndGeoLocationDao {
    @Transaction
    @Query("SELECT * FROM Tree")
    fun getTreeAndGeoLocation(): TreeAndGeoLocation
}