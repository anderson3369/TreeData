package com.orchardlog.treedata.utils

import android.content.Context
import androidx.startup.Initializer
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class AuthInitializer : Initializer<FirebaseAuth> {

    override fun create(context: Context): FirebaseAuth {
        return Firebase.auth
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}
