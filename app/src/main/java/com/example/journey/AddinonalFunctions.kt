package com.example.journey

import android.content.Context
import android.database.Cursor
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import java.io.File
import java.nio.charset.Charset

data class Coordinate(val latitude: String, val longitude: String)

fun writeJSONtoFile(file:File, latitude: String, longitude: String){

    val coordinate = Coordinate(latitude, longitude)
    Log.i(LOG_TAG, "data from object " + coordinate.latitude + coordinate.longitude)
    Log.i(LOG_TAG, "object " + coordinate.toString())
    val json = Gson().toJson(coordinate)
    Log.d(LOG_TAG, "json " + json)
    file.writeText(json, Charset.defaultCharset())
}

fun readJsonFromFile(file: File, context: Context): String{

    val json = file.readText(Charset.defaultCharset())
    Toast.makeText(context, "test 2" + json.toString(), Toast.LENGTH_LONG).show()
    Log.i(LOG_TAG, "json from file " + json)
    val temp = Gson().fromJson(json, Coordinate::class.java)
    Log.i(LOG_TAG, "object from file " + temp.toString())
    return temp.toString()
}


fun getList(c: Cursor?): List<Any>{

    val l = mutableListOf<Any>()
    if (c != null){
        if(c.moveToFirst()){
            do{
                for (cn in c.columnNames) {
                    l.add(c.getString(c.getColumnIndex(cn)))
                }
            }while (c.moveToNext())
        }
    }
    else Log.d(LOG_TAG, "Cursor is null")
    return l
}

fun getMap(c: Cursor?): Map<String, List<Double>>{

    val m = mutableMapOf<String, List<Double>>()
    if (c != null){
        if(c.moveToFirst()){
            do{
                lateinit var name:String
                val l = mutableListOf<Double>()
                for (cn in c.columnNames) {
                    if (cn == "name"){
                        name = c.getString(c.getColumnIndex(cn))
                    }
                else{
                        l.add(c.getString(c.getColumnIndex(cn)).toDouble())
                    }
                }
                m.put(name, l)
            }while (c.moveToNext())
        }
    }
    else Log.d(LOG_TAG, "Cursor is null")
    return m
}