package com.orchardmanager.treedata.di

import android.content.Context
import com.orchardmanager.treedata.daos.FarmDao
import com.orchardmanager.treedata.daos.FarmerDao
import com.orchardmanager.treedata.data.OrchardDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {

    @Singleton
    @Provides
    fun provideOrchardDatabase(@ApplicationContext context: Context): OrchardDatabase {
        return OrchardDatabase.getInstance(context)
    }

    //Bound to multiple items; temporary fix
    //@Provides
    //fun provideFarmerDao(orchardDatabase: OrchardDatabase):FarmerDao {
    //    return  orchardDatabase.farmerDao()
    //}

    @Provides
    fun provideFarmDao(orchardDatabase: OrchardDatabase):FarmDao {
        return orchardDatabase.farmDao()
    }


}