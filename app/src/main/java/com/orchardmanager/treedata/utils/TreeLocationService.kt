package com.orchardmanager.treedata.utils

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

class TreeLocationService: Service() {

    private val mBinder = LocalBinder()
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private var mLocationRequest: LocationRequest? = null

    override fun onCreate() {
        super.onCreate()
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        stopForeground(STOP_FOREGROUND_DETACH)
        return mBinder
    }

    inner class LocalBinder : Binder() {
        internal val service: TreeLocationService
            get() = this@TreeLocationService
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
    }
}