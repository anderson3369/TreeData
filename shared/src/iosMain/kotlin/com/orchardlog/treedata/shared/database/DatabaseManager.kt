package com.orchardlog.treedata.shared.database

import com.orchardlog.treedata.shared.getDatabaseBuilder

/**
 * Singleton manager to provide the Room database instance to iOS (Swift).
 */
object DatabaseManager {
    private val databaseInstance: OrchardDatabase by lazy {
        getRoomDatabase(getDatabaseBuilder())
    }

    fun getDatabase(): OrchardDatabase = databaseInstance
}
