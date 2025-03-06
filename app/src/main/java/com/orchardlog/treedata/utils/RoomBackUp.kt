package com.orchardlog.treedata.utils

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.orchardlog.treedata.data.OrchardDatabase
import kotlinx.coroutines.tasks.await
import java.io.File

class RoomBackUp {

    companion object {
        private const val TAG = "RoomBackUp"
        private val storageRef = Firebase.storage.reference
        //private val uid = Firebase.auth.currentUser?.uid
        //private var path = storageRef.child("orchardLog/")
        private var isFirstTime = false

        fun backupDatabase(context: Context, dateTime: Long, uid: String?) {

            val dbFile = File(context.getDatabasePath(DATABASE_NAME).path)
            val dbFileW = File(context.getDatabasePath(DATABASE_NAME + "-wal").path)
            val backupFile = File(context.filesDir, "backup:$dateTime.db")
            val backupFileW = File(context.filesDir, "backup:$dateTime.db-wal")

            try {
                dbFile.copyTo(backupFile)
                dbFileW.copyTo(backupFileW)
            } catch (e: Exception) {
                when (e) {
                    //Unavoidable
                    is FileAlreadyExistsException -> {
                        Log.i(TAG, "File already exists")
                    }
                }
            }
            val dbRef = storageRef.child("orchardLog/" + uid.toString() + "/backup:$dateTime.db")
            val dbRefW = storageRef.child("orchardLog/" + uid.toString() + "/backup:$dateTime.db-wal")
            val uploadTask = dbRef.putFile(backupFile.toUri())
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                dbRefW.putFile(backupFileW.toUri())
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // File uploaded successfully
                    Log.i(TAG, "Upload successful")
                } else {
                    // Handle unsuccessful uploads
                }
            }

            removeOldDatabase(uid)
        }

        private fun removeOldDatabase(uid: String?) {
            val path = storageRef.child("orchardLog/" + uid.toString())
            path.listAll().addOnSuccessListener { listResult ->
                if(listResult.items.size < 2) {
                    return@addOnSuccessListener
                }

                if(listResult.items.size > 2) {
                    listResult.items.forEach { item ->
                        val itemRef = path.child(item.name)
                        val parseName = item.name.split(":")
                        val dateTime = parseName[1].split(".")

                        listResult.items.forEach { file ->
                            val paresFile = file.name.split(":")
                            val dateFile = paresFile[1].split(".")
                            if(dateTime.toString().toLong() < dateFile.toString().toLong()) {
                                itemRef.delete()
                            }
                        }
                    }
                }
            }
        }

        suspend fun isFirstTime(uid: String?): Boolean {
            val path = storageRef.child("orchardLog/" + uid.toString())
            val task = path.listAll().addOnSuccessListener {
                listResult ->
                if(listResult.items.size < 2) {
                    isFirstTime = true
                    return@addOnSuccessListener
                }
            }
            task.await()
            return isFirstTime
        }

        fun restoreDatabase(context: Context, uid: String?): Boolean {
            val path = storageRef.child("orchardLog/" + uid.toString())
            OrchardDatabase.destroy()
            var isSuccessful = false

            path.listAll().addOnSuccessListener {
                listResult ->
                if(listResult.items.size < 2) {
                    isFirstTime = true
                    return@addOnSuccessListener
                }
                val dbFile = File(context.getDatabasePath(DATABASE_NAME).path)
                val dbFileW = File(context.getDatabasePath(DATABASE_NAME + "-wal").path)

                val localFileDb = File.createTempFile(DATABASE_NAME,"db")
                val localFileDbW = File.createTempFile(DATABASE_NAME,"db-wal")
                if(listResult.items.size == 2) {
                    listResult.items.forEach {
                        item ->
                        //val itemRef = path.child(item.name)
                        val parseName = item.name.split(":")
                        val dateTime = parseName[1].split(".")
                        val ext = dateTime[1]
                        if(ext == "db") {
                            localFileDb.renameTo(dbFile)
                            item.getFile(localFileDb).addOnSuccessListener {
                                    download ->
                                if(dbFile.delete()) {
                                    localFileDb.copyTo(dbFile)
                                    isSuccessful = true
                                }
                            }.addOnFailureListener {
                                    failure ->
                                Log.e(TAG, failure.toString())
                            }
                        } else if(ext == "db-wal") {
                            localFileDbW.renameTo(dbFileW)
                            item.getFile(localFileDbW).addOnSuccessListener { download ->
                                if (dbFileW.delete()) {
                                    localFileDbW.copyTo(dbFileW)
                                    isSuccessful = true
                                }
                            }.addOnFailureListener {
                                failure ->
                                Log.e(TAG, failure.toString())
                            }

                        }
                    }
                } else {
                    listResult.items.forEach {
                        item ->
                        //val itemRef = path.child(item.name)
                        val parseName = item.name.split(":")
                        val dateTime = parseName[1].split(".")

                        listResult.items.forEach {
                            file ->
                            val paresFile = file.name.split(":")
                            val dateFile = paresFile[1].split(".")
                            if( dateFile[0].toLong() > dateTime[0].toLong()) {
                                val ext = dateFile[1]
                                if(ext == "db") {
                                    localFileDb.renameTo(dbFile)
                                    file.getFile(localFileDb).addOnSuccessListener {
                                            download ->
                                        if(dbFile.delete()) {
                                            localFileDb.copyTo(dbFile)
                                            isSuccessful = true
                                        }
                                    }.addOnFailureListener {
                                            failure ->
                                        Log.e(TAG, failure.toString())
                                    }
                                } else if(ext == "db-wal") {
                                    localFileDbW.renameTo(dbFileW)
                                    file.getFile(localFileDbW).addOnSuccessListener { download ->
                                        if (dbFileW.delete()) {
                                            localFileDbW.copyTo(dbFileW)
                                            isSuccessful = true
                                        }
                                    }.addOnFailureListener {
                                            failure ->
                                        Log.e(TAG, failure.toString())
                                    }
                                }
                            }
                        }
                    }
                }
            }.addOnFailureListener {
                failure ->
                Log.e(TAG, failure.toString())
            }
            OrchardDatabase.getInstance(context)
            return isSuccessful
        }
    }

}