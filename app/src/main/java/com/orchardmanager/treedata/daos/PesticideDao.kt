package com.orchardmanager.treedata.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.orchardmanager.treedata.entities.Pesticide
import kotlinx.coroutines.flow.Flow

@Dao
interface PesticideDao {
    @Insert
    suspend fun insert(pesticide: Pesticide): Long

    @Update
    fun update(pesticide: Pesticide)

    @Delete
    fun delete(pesticide: Pesticide)

    @Query("SELECT * FROM Pesticide")
    fun getPesticides(): Flow<MutableList<Pesticide>>
}