package com.orchardlog.treedata.shared.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardlog.treedata.shared.model.TreeWithDiseases

@Dao
interface TreeWithDiseasesDao {
    @Transaction
    @Query("SELECT * FROM Tree")
    suspend fun getTreeWithDiseases(): List<TreeWithDiseases>
}
