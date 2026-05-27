package com.orchardlog.treedata.shared

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.orchardlog.treedata.shared.database.OrchardDatabase

class OrchardDB {

    companion object {
        fun getDatabaseBuilder(ctx: Context): RoomDatabase.Builder<OrchardDatabase> {
            val appContext = ctx.applicationContext
            val dbFile = appContext.getDatabasePath(DATABASE_NAME)
            return Room.databaseBuilder<OrchardDatabase>(
                context = appContext,
                name = dbFile.absolutePath
            )
        }
    }

}