package com.orchardmanager.treedata.repositories

import com.orchardmanager.treedata.daos.OrchardWithTreesDao
import com.orchardmanager.treedata.daos.RootstockDao
import com.orchardmanager.treedata.daos.TreeDao
import com.orchardmanager.treedata.daos.VarietyDao
import com.orchardmanager.treedata.entities.Rootstock
import com.orchardmanager.treedata.entities.Tree
import com.orchardmanager.treedata.entities.Variety
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

    fun getAllTrees() = treeDao.getAllTrees()

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
}