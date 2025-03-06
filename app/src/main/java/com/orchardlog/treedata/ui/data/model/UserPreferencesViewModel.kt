package com.orchardlog.treedata.ui.data.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.orchardlog.treedata.repositories.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserPreferencesViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    fun getCanBackup(): LiveData<Boolean> {
        return userPreferencesRepository.canBackup.asLiveData()
    }

    fun getBackupDate(): LiveData<Long> {
        return userPreferencesRepository.backupDate.asLiveData()
    }

   fun getIsFirstTime(): LiveData<Boolean> {
       return userPreferencesRepository.isFirstTime.asLiveData()
   }

    suspend fun setBackup(isBackup: Boolean) {
        userPreferencesRepository.setBackup(isBackup)
    }

    suspend fun setBackupDate() {
        userPreferencesRepository.seBackupDate()
    }

    suspend fun setFirstTime(isFirstTime: Boolean) {
        userPreferencesRepository.setIsFirstTime(isFirstTime)
    }
}
