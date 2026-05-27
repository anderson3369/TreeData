package com.orchardlog.treedata.shared

import androidx.room.Room
import androidx.room.RoomDatabase
import com.orchardlog.treedata.shared.database.OrchardDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

fun getDatabaseBuilder(): RoomDatabase.Builder<OrchardDatabase> {
    val dbFilePath = documentDirectory() + "/$DATABASE_NAME.db"
    return Room.databaseBuilder<OrchardDatabase>(
        name = dbFilePath
    )
}

@kotlinx.cinterop.ExperimentalForeignApi
private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory?.path)
}
