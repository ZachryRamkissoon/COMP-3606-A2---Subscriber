package com.example.maplab

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

const val DB_NAME = "database.sql"
const val DB_VERSION = 1

class DatabaseHelper(context: Context, factory: SQLiteDatabase.CursorFactory?):SQLiteOpenHelper(context, DB_NAME,factory, DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        val createLocationTableQuery = ("CREATE TABLE Location (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "latitude DOUBLE," +
                "longitude DOUBLE," +
                "studentID TEXT," +
                "speed DOUBLE," +
                "timestamp INTEGER)")

        db.execSQL(createLocationTableQuery)
        println("Db Created")
    }

    // WHen upgrading DB versions, we have to carefully add columns in a non destructive way
    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        // Content similar to enterprise DB
    }

    fun createLocation(latitude: Double, longitude: Double, id: String, timestamp: Int, speed: Double){
        val values = ContentValues()

        values.put("latitude", latitude)
        values.put("longitude", longitude)
        values.put("studentID", id)
        values.put("timestamp", timestamp)
        values.put("speed", speed)

        val db = this.writableDatabase
        db.insert("Location",null, values)
        db.close()
        println("Success")
    }

    fun getAllLocationsGroupedByStudent(): Map<String, List<PLocation>> {
        val studentLocationMap = mutableMapOf<String, MutableList<PLocation>>()

        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Location", null)

        if (cursor.moveToFirst()) {
            do {
                val studentID = cursor.getString(cursor.getColumnIndexOrThrow("studentID"))
                val latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude"))
                val longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"))
                val time = cursor.getInt(cursor.getColumnIndexOrThrow("timestamp"))
                val speed = cursor.getDouble(cursor.getColumnIndexOrThrow("speed"))

                val locationData = PLocation(
                    id = studentID,
                    latitude = latitude,
                    longitude = longitude,
                    time = time,
                    speed = speed
                )

                val locationList = studentLocationMap.getOrPut(studentID) { mutableListOf() }
                locationList.add(locationData)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return studentLocationMap
    }

    fun getAllLocations(): List<PLocation>{
        val result: MutableList<PLocation> = mutableListOf()

        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Location", null)

        if(!(!cursor.moveToFirst() || cursor.count == 0)){
            do {
                val studentIDIdx = cursor.getColumnIndex("studentID")
                val latitudeIdx = cursor.getColumnIndex("latitude")
                val longitudeIdx = cursor.getColumnIndex("longitude")
                val timestampIdx = cursor.getColumnIndex("timestamp")
                val idIdx = cursor.getColumnIndex("id")
                val speedIdx = cursor.getColumnIndex("speed")

                if (studentIDIdx > 0 && latitudeIdx > 0 && longitudeIdx > 0 && timestampIdx > 0) {
                    val studentID = cursor.getString(studentIDIdx)
                    val latitude = cursor.getDouble(latitudeIdx)
                    val longitude = cursor.getDouble(longitudeIdx)
                    val timestamp = cursor.getInt(timestampIdx)
                    val speed = cursor.getDouble(speedIdx)
                    result.add(PLocation(studentID, latitude, longitude, timestamp, speed))
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return result
    }

    fun getAllLocations(id: String): List<PLocation>{
        val result: MutableList<PLocation> = mutableListOf()

        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Location WHERE studentID = ?", arrayOf(id), null)

        if(!(!cursor.moveToFirst() || cursor.count == 0)){
            do {
                val studentIDIdx = cursor.getColumnIndex("studentID")
                val latitudeIdx = cursor.getColumnIndex("latitude")
                val longitudeIdx = cursor.getColumnIndex("longitude")
                val timestampIdx = cursor.getColumnIndex("timestamp")
                val speedIdx = cursor.getColumnIndex("speed")
                val idIdx = cursor.getColumnIndex("id")

                if (studentIDIdx > 0 && latitudeIdx > 0 && longitudeIdx > 0 && timestampIdx > 0 && speedIdx > 0) {
                    val studentID = cursor.getString(studentIDIdx)
                    val latitude = cursor.getDouble(latitudeIdx)
                    val longitude = cursor.getDouble(longitudeIdx)
                    val timestamp = cursor.getInt(timestampIdx)
                    val speed = cursor.getDouble(speedIdx)
                    result.add(PLocation(studentID, latitude, longitude, timestamp, speed))
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return result
    }

    fun reset() {
        val db = writableDatabase
        db.execSQL("DROP TABLE IF EXISTS Location")
        val createLocationTableQuery = ("CREATE TABLE Location (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "latitude DOUBLE," +
                "longitude DOUBLE," +
                "studentID TEXT," +
                "speed DOUBLE," +
                "timestamp INTEGER)")

        db.execSQL(createLocationTableQuery)
        db.close()
    }
}