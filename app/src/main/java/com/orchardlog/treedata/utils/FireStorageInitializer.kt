package com.orchardlog.treedata.utils

import android.content.Context
import androidx.startup.Initializer
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage

class FireStorageInitializer : Initializer<FirebaseStorage> {

    override fun create(context: Context): FirebaseStorage {
        return Firebase.storage
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}
