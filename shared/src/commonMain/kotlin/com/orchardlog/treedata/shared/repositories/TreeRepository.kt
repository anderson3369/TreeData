package com.orchardlog.treedata.shared.repositories

import com.orchardlog.treedata.shared.daos.OrchardWithTreesDao
import com.orchardlog.treedata.shared.daos.RootstockDao
import com.orchardlog.treedata.shared.daos.TreeDao
import com.orchardlog.treedata.shared.daos.VarietyDao
import com.orchardlog.treedata.shared.model.Rootstock
import com.orchardlog.treedata.shared.model.Tree
import com.orchardlog.treedata.shared.model.Variety
import com.orchardlog.treedata.shared.TemporalUtils
import kotlinx.coroutines.flow.Flow

class TreeRepository(
    private val treeDao: TreeDao,
    private val orchardWithTreesDao: OrchardWithTreesDao,
    private val rootstockDao: RootstockDao,
    private val varietyDao: VarietyDao
) {

    suspend fun createTree(tree: Tree): Long {
        val now = TemporalUtils.now()
        val treeToInsert = tree.copy(
            validFrom = now,
            validTo = TemporalUtils.INFINITY
        )
        return treeDao.insert(treeToInsert)
    }

    suspend fun updateTree(tree: Tree) {
        val now = TemporalUtils.now()
        
        // 1. Get the current version from the database to ensure we don't overwrite it with new data before closing
        val existingTree = treeDao.getTreeSync(tree.id)
        existingTree?.let {
            // Close current version
            val closedVersion = it.copy(validTo = now)
            treeDao.update(closedVersion)
        }

        // 2. Insert new version with updated data
        val newVersion = tree.copy(
            id = 0L, // New primary key for Room
            validFrom = now,
            validTo = TemporalUtils.INFINITY
        )
        treeDao.insert(newVersion)
    }

    suspend fun deleteTree(tree: Tree) {
        treeDao.delete(tree)
    }

    fun getAllTrees(): Flow<List<Tree>> = treeDao.getAllTrees(TemporalUtils.now())

    fun getTree(id: Long): Flow<Tree?> = treeDao.getTree(id)

    suspend fun getTreeCurrent(persistentId: String): Tree? {
        return treeDao.getTreeByPersistentId(persistentId, TemporalUtils.now())
    }

    suspend fun createRootstock(rootstock: Rootstock): Long {
        return rootstockDao.insert(rootstock)
    }

    suspend fun updateRootstock(rootstock: Rootstock) {
        rootstockDao.update(rootstock)
    }

    suspend fun deleteRootstock(rootstock: Rootstock) {
        rootstockDao.delete(rootstock)
    }

    fun getAllRootstocks(): Flow<List<Rootstock>> = rootstockDao.getRootstocks()

    fun getRootstocksMap(): Flow<Map<Long, String>> = rootstockDao.getRootstocksMap()

    suspend fun createVariety(variety: Variety): Long {
        return varietyDao.insert(variety)
    }

    suspend fun updateVariety(variety: Variety) {
        varietyDao.update(variety)
    }

    suspend fun deleteVariety(variety: Variety) {
        varietyDao.delete(variety)
    }

    fun getAllVarieties(): Flow<List<Variety>> = varietyDao.getVarieties()

    fun getVarietiesMap(): Flow<Map<Long, String>> = varietyDao.getVarietiesMap()
}
