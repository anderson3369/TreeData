package com.orchardlog.treedata.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardlog.treedata.entities.TreeWithDiseases

@Dao
interface TreeWithDiseasesDao {
    @Transaction
    @Query("SELECT * FROM Tree")
    fun getTreeWithDiseases(): List<TreeWithDiseases>
}