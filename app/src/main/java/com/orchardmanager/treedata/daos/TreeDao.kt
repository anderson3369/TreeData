package com.orchardmanager.treedata.daos

import androidx.room.*
import com.orchardmanager.treedata.entities.Tree

@Dao
interface TreeDao {
    @Insert
    fun insert(tree: Tree)

    @Update
    fun update(tree: Tree)

    @Delete
    fun delete(tree: Tree)

    @Query("SELECT * FROM Tree")
    fun getTrees(): List<Tree>
}