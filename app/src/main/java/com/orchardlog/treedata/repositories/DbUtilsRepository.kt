package com.orchardlog.treedata.repositories

import androidx.sqlite.db.SimpleSQLiteQuery
import com.orchardlog.treedata.daos.DbUtilsDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DbUtilsRepository @Inject constructor (private val dbUtilsDao: DbUtilsDao) {

    fun checkPoint() = dbUtilsDao.checkPoint(SimpleSQLiteQuery("pragma wal_checkpoint(full)"))
}