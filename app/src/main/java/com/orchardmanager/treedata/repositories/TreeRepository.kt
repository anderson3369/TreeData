package com.orchardmanager.treedata.repositories

import com.orchardmanager.treedata.daos.OrchardWithTreesDao
import com.orchardmanager.treedata.daos.TreeDao
import com.orchardmanager.treedata.entities.Tree
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TreeRepository @Inject constructor(private val treeDao: TreeDao,
    private val orchardWithTreesDao: OrchardWithTreesDao) {
    suspend fun createTree(tree: Tree): Long {
        return treeDao.insert(tree)
    }

    fun updateTree(tree: Tree) {
        treeDao.update(tree)
    }

    fun getAllTrees() = treeDao.getAllTrees()

    fun getOrchardWithTrees(id: Long) = orchardWithTreesDao.getOrchardWithTrees(id)
}