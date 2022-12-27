package com.orchardmanager.treedata.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.orchardmanager.treedata.entities.Farmer
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmerDao {
    @Insert
    suspend fun insert(farmer: Farmer): Long

    @Update
    fun update(farmer: Farmer)

    @Delete
    fun delete(farmer: Farmer)

    @Query("SELECT * FROM Farmer")
    fun getFarmers(): Flow<MutableList<Farmer>>

    //Convience method for now
    @Query("SELECT id FROM Farmer LIMIT 1")
    fun getFarmerId(): Flow<Long>
}