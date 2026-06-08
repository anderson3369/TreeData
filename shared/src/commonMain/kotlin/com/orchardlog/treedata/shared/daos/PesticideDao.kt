package com.orchardlog.treedata.shared.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.orchardlog.treedata.shared.model.Pesticide
import kotlinx.coroutines.flow.Flow

@Dao
interface PesticideDao {
    @Insert
    suspend fun insert(pesticide: Pesticide): Long

    @Update
    suspend fun update(pesticide: Pesticide)

    @Delete
    suspend fun delete(pesticide: Pesticide)

    @Query("SELECT * FROM Pesticide")
    fun getPesticides(): Flow<List<Pesticide>>
}
