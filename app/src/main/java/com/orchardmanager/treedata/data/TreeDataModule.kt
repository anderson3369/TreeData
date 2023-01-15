package com.orchardmanager.treedata.data

import android.content.Context
import androidx.room.Room
import com.orchardmanager.treedata.daos.*
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
    //@Named("orchardDatabase")
    fun provideOrchardDatabase(@ApplicationContext context: Context) : OrchardDatabase {

        return OrchardDatabase.getInstance(context)
    }

    @Provides
    fun provideFarmDao(orchardDatabase: OrchardDatabase): FarmDao {
        return orchardDatabase.farmDao()
    }

    @Provides
    fun provideOrchardDao(orchardDatabase: OrchardDatabase): OrchardDao {
        return orchardDatabase.orchardDao()
    }

    @Provides
    fun provideFarmerDao(orchardDatabase: OrchardDatabase) : FarmerDao {
        return orchardDatabase.farmerDao()
    }

    @Provides
    fun provideFarmWithOrchardsDao(orchardDatabase: OrchardDatabase): FarmWithOrchardsDao {
        return orchardDatabase.farmWithOrchardsDao()
    }

    @Provides
    fun provideFarmerWithFarmDao(orchardDatabase: OrchardDatabase): FarmerWithFarmDao {
        return orchardDatabase.farmerWithFarmDao()
    }

    @Provides
    fun provideTreeDao(orchardDatabase: OrchardDatabase): TreeDao {
        return orchardDatabase.treeDao()
    }

    @Provides
    fun provideOrchardWithTreesDao(orchardDatabase: OrchardDatabase): OrchardWithTreesDao {
        return orchardDatabase.orchardWithTreesDao()
    }


}