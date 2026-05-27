package com.orchardlog.treedata

import android.app.Application
import com.orchardlog.treedata.shared.viewmodels.ViewModelProvider
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TreeDataApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        ViewModelProvider.initialize(this)
    }
}
