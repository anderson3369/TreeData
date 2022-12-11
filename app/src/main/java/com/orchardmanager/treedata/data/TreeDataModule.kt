package com.orchardmanager.treedata.data

import android.content.Context
import com.orchardmanager.treedata.daos.FarmerDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class TreeDataModule {

    @Provides
    @Singleton
    @Named("orchardDatabase")
    fun provideOrchardDatabase(@ApplicationContext context: Context) : OrchardDatabase {
        return OrchardDatabase.getInstance(context)
    }

    @Provides
    fun provideFarmerDao(orchardDatabase: OrchardDatabase) : FarmerDao {
        return orchardDatabase.farmerDao()
    }


}