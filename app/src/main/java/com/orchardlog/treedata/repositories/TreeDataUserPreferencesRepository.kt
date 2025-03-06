package com.orchardlog.treedata.repositories

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

interface UserPreferencesRepository {
    val canBackup: Flow<Boolean>
    val backupDate: Flow<Long>
    val isFirstTime: Flow<Boolean>
    suspend fun setBackup(isBackup: Boolean)
    suspend fun seBackupDate()
    suspend fun setIsFirstTime(isFirstTime: Boolean)
}

@Singleton
class TreeDataUserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
): UserPreferencesRepository {
    private val DATETIME = longPreferencesKey("datetime")
    private val BACKUP = booleanPreferencesKey("backup")
    private val ISFIRSTTIME = booleanPreferencesKey("isFirstTime")

    companion object {
        const val TAG = "TreeDataUserPreferencesRepository"
    }

    override val canBackup = dataStore.data.catch { exception ->
            if (exception is IOException) {
                Log.i(TAG, "Error reading preferences.", exception)
            } else {
                throw exception
            }
    }.map { preferences ->
            preferences[BACKUP] ?: false
    }


    override val backupDate = dataStore.data.catch { exception ->
            if (exception is IOException) {
                Log.i(TAG, "Error reading preferences.", exception)
            } else {
                throw exception
            }
    }.map { preferences ->
            preferences[DATETIME] ?: 0L
    }

    override val isFirstTime = dataStore.data.catch { exception ->
        if (exception is IOException) {
            Log.i(TAG, "Error reading preferences.", exception)
        } else {
            throw exception
        }
    }.map { preferences ->
        preferences[ISFIRSTTIME] ?: true
    }


    override suspend fun setBackup(isBackup: Boolean) {
        dataStore.edit { settings ->
            settings[BACKUP] = isBackup
        }
    }

    override suspend fun seBackupDate() {
        dataStore.edit { settings ->
            val currentCounterValue = LocalDateTime.now().toEpochSecond(java.time.ZoneOffset.UTC)
            settings[DATETIME] = currentCounterValue
        }
    }

    override suspend fun setIsFirstTime(isFirstTime: Boolean) {
        dataStore.edit { settings ->
            settings[ISFIRSTTIME] = isFirstTime
        }
    }

}