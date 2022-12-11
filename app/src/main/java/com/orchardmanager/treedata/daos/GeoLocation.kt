package com.orchardmanager.treedata.daos

import androidx.room.*
import com.orchardmanager.treedata.entities.GeoLocation

@Dao
interface GeoLocation {
    @Insert
    fun insert(geoLocation: GeoLocation)

    @Update
    fun update(geoLocation: GeoLocation)

    @Delete
    fun delete(geoLocation: GeoLocation)

    @Query("SELECT * FROM GeoLocation")
    fun getTreeLocations(): List<GeoLocation>
}