package com.orchardlog.treedata.entities

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(foreignKeys = [
    ForeignKey(entity = Farmer::class, parentColumns = ["id"], childColumns = ["farmerId"])],
    indices = [Index("farmerId")])
data class Farm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val farmerId: Long = 0L,
    val name: String,
    val siteId: String
): Parcelable {

    protected constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString().toString(),
        parcel.readString().toString()
    )

    override fun toString():String = name + "  -  " + siteId

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(farmerId)
        parcel.writeString(name)
        parcel.writeString(siteId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<Farm> {
        override fun createFromParcel(parcel: Parcel): Farm {
            return Farm(parcel)
        }

        override fun newArray(size: Int): Array<Farm?> {
            return arrayOfNulls(size)
        }
    }
}
