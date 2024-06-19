package com.orchardlog.treedata.data

import androidx.room.TypeConverter
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint

class GeoPointConverter {

    @TypeConverter
    fun toGeoPoint(latitude: Double?, longitude: Double?): IGeoPoint {
        return GeoPoint(latitude!!, longitude!!)
    }

   @TypeConverter
   fun fromGeoPoint(geoPoint: IGeoPoint): Array<Double> {
       val lat = geoPoint.latitude
       val lng = geoPoint.longitude
       return arrayOf(lat, lng)
   }
}