package com.orchardmanager.treedata.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardmanager.treedata.entities.TreeWithDiseases

@Dao
interface TreeWithDiseasesDao {
    @Transaction
    @Query("SELECT * FROM Tree")
    fun getTreeWithDiseases(): List<TreeWithDiseases>
}