package com.orchardlog.treedata.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.orchardlog.treedata.repositories.TreeDataUserPreferencesRepository
import com.orchardlog.treedata.repositories.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    // just my preference of naming including the package name
    name = "com.orchardlog.treedata.user_preferences"
)

@Module
@InstallIn(SingletonComponent::class)
abstract class UserPreferencesModule {

    //@Provides
    //fun providesUserPreferencesViewModel(userPreferencesRepository: UserPreferencesRepository): UserPreferencesViewModel {
        //return UserPreferencesViewModel(userPreferencesRepository)
    //}

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        treeDataUserPreferencesRepository: TreeDataUserPreferencesRepository
    ): UserPreferencesRepository

    companion object {
        @Provides
        @Singleton
        fun provideDataStorePreferences(@ApplicationContext context: Context): DataStore<Preferences> {
            return context.dataStore
        }

    }

}