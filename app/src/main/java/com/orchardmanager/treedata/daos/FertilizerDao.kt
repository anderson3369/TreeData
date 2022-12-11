package com.orchardmanager.treedata.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.orchardmanager.treedata.entities.Fertilizer

@Dao
interface FertilizerDao {
    @Insert
    fun insert(fertilizer: Fertilizer)

    @Update
    fun update(fertilizer: Fertilizer)

    @Delete
    fun delete(fertilizer: Fertilizer)

    @Query("SELECT * FROM Fertilizer")
    fun getFertilizers(): LiveData<List<Fertilizer>>
}