package com.orchardlog.treedata.repositories

import com.orchardlog.treedata.daos.OrchardWithTreesDao
import com.orchardlog.treedata.daos.RootstockDao
import com.orchardlog.treedata.daos.TreeDao
import com.orchardlog.treedata.daos.VarietyDao
import com.orchardlog.treedata.entities.Rootstock
import com.orchardlog.treedata.entities.Tree
import com.orchardlog.treedata.entities.Variety
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TreeRepository @Inject constructor(private val treeDao: TreeDao,
    private val orchardWithTreesDao: OrchardWithTreesDao,
    private val rootstockDao: RootstockDao,
    private val varietyDao: VarietyDao) {

    suspend fun createTree(tree: Tree): Long {
        return treeDao.insert(tree)
    }

    fun updateTree(tree: Tree) {
        treeDao.update(tree)
    }

    fun deleteTree(tree: Tree) {
        treeDao.delete(tree)
    }

    fun getAllTrees() = treeDao.getAllTrees()

    fun getTree(id: Long) = treeDao.getTree(id)

    fun getOrchardWithTrees(id: Long) = orchardWithTreesDao.getOrchardWithTrees(id)

    fun getAllOrchardWithTrees() = orchardWithTreesDao.getAllOrchardWithTrees()

    suspend fun createRootstock(rootstock: Rootstock): Long {
        return rootstockDao.insert(rootstock)
    }

    fun updateRootstock(rootstock: Rootstock) {
        rootstockDao.update(rootstock)
    }

    fun deleteRootstock(rootstock: Rootstock) {
        rootstockDao.delete(rootstock)
    }

    fun getAllRootstocks() = rootstockDao.getRootstocks()

    fun getRootstocksMap() = rootstockDao.getRootstocksMap()

    suspend fun createVariety(variety: Variety): Long {
        return varietyDao.insert(variety)
    }

    fun updatevVariety(variety: Variety) {
        varietyDao.update(variety)
    }

    fun deleteVariety(variety: Variety) {
        varietyDao.delete(variety)
    }

    fun getAllVarieties() = varietyDao.getVarieties()

    fun getVarietiesMap() = varietyDao.getVarietiesMap()
}