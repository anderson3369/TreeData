package com.orchardmanager.treedata.daos

import androidx.room.*
import com.orchardmanager.treedata.entities.Tree
import kotlinx.coroutines.flow.Flow

@Dao
interface TreeDao {
    @Insert
    suspend fun insert(tree: Tree): Long

    @Update
    fun update(tree: Tree)

    @Delete
    fun delete(tree: Tree)

    @Query("SELECT * FROM Tree")
    //@TypeConverters(GeoPointConverter::class)
    fun getAllTrees(): Flow<MutableList<Tree>>

    @Query("SELECT * FROM Tree WHERE id = :id")
    fun getTree(id: Long): Flow<Tree>

    @Query("SELECT * FROM Tree WHERE orchardId = :orchardId")
    fun getTrees(orchardId: Long): Flow<MutableList<Tree>>
}