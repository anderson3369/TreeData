package com.orchardmanager.treedata.daos

import android.database.sqlite.SQLiteException
import androidx.room.*
import com.orchardmanager.treedata.entities.Pump
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface PumpDao {
    @Insert
    @Throws(SQLiteException::class)
    suspend fun insert(pump: Pump): Long

    @Update
    fun update(pump: Pump)

    @Delete
    fun delete(pump: Pump)

    @Query("SELECT * FROM Pump")
    fun getPumps(): Flow<MutableList<Pump>>

    @MapInfo(keyColumn = "id")
    @Query("SELECT * FROM Pump")
    fun getPumpMap(): Flow<Map<Long, Pump>>
}