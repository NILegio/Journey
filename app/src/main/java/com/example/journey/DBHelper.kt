package com.example.journey

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException


const val LOG_TAG = "myLogs"
const val DB_NAME = "timetable.db"
const val DB_VERSION = 1


class DBHelper(context: Context): SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION){

    private val resources = context.resources

    private val listId = listOf(R.array.stationlist, R.array.transportlist)
    private val listNames = listOf("route", "transport")
    private val listXmlId = listOf(R.xml.timetable_1545, R.xml.timetable_429)

    override fun onCreate(db: SQLiteDatabase) {

        Log.d(LOG_TAG, "--- onCreate database ---")

        db.execSQL("create table timetable(" +
                "id integer primary key autoincrement," +
                "transport_id integer NOT NULL," +
                "start_route_id int not null," +
                "end_route_id int not null," +
                "time_arrive time not NULL," +
                "timetotravel int DEFAULT 0," +
                "day text not null," +
                "FOREIGN KEY (end_route_id) REFERENCES route (id)" +
                "FOREIGN KEY (start_route_id) REFERENCES route (id)" +
                "FOREIGN KEY (transport_id) REFERENCES transport (id))")

        db.execSQL("create table transport("+
                "id integer primary key autoincrement,"+
                "name VARCHAR(100))"
        )

        db.execSQL("create table route("+
                "id integer primary key autoincrement,"+
                "name VARCHAR(100))"
        )

        for (i in listId.indices){
            simpleinsert(resources.getStringArray(listId[i]),db, listNames[i])
        }

        for (id in listXmlId){
            notsosimpleinsert(db, id)
        }
    }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }


    private fun simpleinsert(array:Array<String>, db: SQLiteDatabase, name:String){
        val cv = ContentValues()
        for (i in array.indices){
            cv.put("name", array[i])
            db.insert(name, null, cv)
        }
    }


    private fun notsosimpleinsert(db: SQLiteDatabase, id:Int){

        val _xml = resources.getXml(id)
        val cv = ContentValues()

        try{
            var eventType = _xml.eventType
            while (eventType != XmlPullParser.END_DOCUMENT){
                if((eventType == XmlPullParser.START_TAG)&&(_xml.name.equals("record"))){
                    val transportId = _xml.getAttributeValue(2)//2
                    val startRouteId =_xml.getAttributeValue(3)//3
                    val endRouteId =_xml.getAttributeValue(1)//1
                    val timeArrive =_xml.getAttributeValue(4)//4
                    val day = _xml.getAttributeValue(0)//0
                    cv.put("transport_id", transportId)
                    cv.put("start_route_id", startRouteId)
                    cv.put("end_route_id", endRouteId)
                    cv.put("time_arrive", timeArrive)
                    cv.put("day", day)
                    db.insert("timetable", null, cv)
                }
                eventType = _xml.next()
            }
        }catch (ex: XmlPullParserException){
            Log.e(LOG_TAG, "${ex.cause} error: ${ex.message}")
        }catch (ex: IOException){
            Log.e(LOG_TAG, "${ex.cause} error: ${ex.message}")
        }
        finally {
            _xml.close()
        }
    }
}
