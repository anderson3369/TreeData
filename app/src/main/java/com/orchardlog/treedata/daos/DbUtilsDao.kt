package com.orchardlog.treedata.daos

import androidx.room.Dao
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
interface DbUtilsDao {

    @RawQuery
    fun checkPoint(query: SupportSQLiteQuery): Int

}