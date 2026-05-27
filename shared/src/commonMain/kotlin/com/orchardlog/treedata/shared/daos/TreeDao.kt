package com.orchardlog.treedata.shared.daos

import androidx.room.*
import com.orchardlog.treedata.shared.model.Tree
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface TreeDao {
    @Insert
    suspend fun insert(tree: Tree): Long

    @Update
    suspend fun update(tree: Tree)

    @Delete
    suspend fun delete(tree: Tree)

    @Query("SELECT * FROM Tree WHERE (validTo IS NULL OR validTo > :now)")
    fun getAllTrees(now: Instant): Flow<List<Tree>>

    @Query("SELECT * FROM Tree WHERE id = :id")
    fun getTree(id: Long): Flow<Tree?>

    @Query("SELECT * FROM Tree WHERE id = :id")
    suspend fun getTreeSync(id: Long): Tree?

    @Query("SELECT * FROM Tree WHERE orchardId = :orchardId AND (validTo IS NULL OR validTo > :now)")
    fun getTrees(orchardId: Long, now: Instant): Flow<List<Tree>>

    @Query("SELECT * FROM Tree WHERE persistentId = :persistentId AND (validFrom <= :asOf AND (validTo IS NULL OR validTo > :asOf))")
    suspend fun getTreeByPersistentId(persistentId: String, asOf: Instant): Tree?
}
