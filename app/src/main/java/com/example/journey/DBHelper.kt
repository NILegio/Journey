package com.example.journey


import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader


const val LOG_TAG = "myLogs"
const val DB_NAME = "timetable.db"
const val DB_VERSION = 1


class DBHelper(context: Context): SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION){

    private val assets = context.assets
    private val resources = context.resources

    private val file_list = listOf("429_Komm-Priluki.csv", "429_Minsk-Priluki.csv", "429_Priluki-Minsk.csv",
        "Priluki-SW.csv", "SW-Priluki.csv")

    val lists = getLists(file_list)

    override fun onCreate(db: SQLiteDatabase) {

        Log.d(LOG_TAG, "--- onCreate database ---")

        db.execSQL("create table timetable(" +
                "id integer primary key autoincrement," +
                "transport_id integer NOT NULL," +
                "station_depart_id int not null," +
                "station_arrive_id int not null," +
                "time_depart time not NULL," +
                "time_arrive time DEFAULT 0," +
                "day text not null," +
                "FOREIGN KEY (station_arrive_id) REFERENCES station (id)" +
                "FOREIGN KEY (station_depart_id) REFERENCES station (id)" +
                "FOREIGN KEY (transport_id) REFERENCES transport (id))")

        db.execSQL("create table transport("+
                "id integer primary key autoincrement,"+
                "name VARCHAR(100))"
        )

        db.execSQL("create table station("+
                "id integer primary key autoincrement,"+
                "name VARCHAR(100))"
        )

        for ((key, value) in lists){
            simpleinsert(value, db, key)
        }
           val c: Cursor = db.rawQuery("select name from station", null)
           val array = getList(c)
           c.close()
        justsimpleinsert(db, file_list, array)
       }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }


    private fun simpleinsert(list:List<String>, db: SQLiteDatabase, name:String){
        val cv = ContentValues()
        for (i in list.indices){
            cv.put("name", list[i])
            db.insert(name, null, cv)
        }
    }


    private fun justsimpleinsert(db:SQLiteDatabase, file_list:List<String>, array:List<String>){

        val cv = ContentValues()

        for (name in file_list){
            val file = assets.open(name)
            val rows: List<List<String>> = csvReader().readAll(file)
            for (r_row in rows)
            {
                val row = r_row[0].split(";")
                cv.put("transport_id", row[0])
                cv.put("station_depart_id", array.indexOf(row[1]))
                cv.put("time_depart", row[2])
                cv.put("station_arrive_id", array.indexOf(row[3]))
                cv.put("time_arrive", row[4])
                if (row[5] == "пн вт ср чт пт"){  cv.put("day", "workday")  }
                else { cv.put("day", "holiday")  }
                Log.d(LOG_TAG, cv.toString())
                db.insert("timetable", null, cv)
            }
        }
    }

    fun getLists(file_list: List<String>):Map<String, List<String>>{
        //заставить себя попробывать сделать нормальные csv файлы с юникодом и разделителями
        //запятыми, главное - копировать в юникод кодировке, а потом с помощью прорграмм делать
        // разделители запятые и заголовки

        val station_list = mutableListOf<String>()
        val transport_list = mutableListOf<String>()

        for (name in file_list){
            val file = assets.open(name)
            val rows: List<List<String>> = csvReader().readAll(file)
            for (r_row in rows){
                val row = r_row[0].split(";")
                if (row[0] !in transport_list) {transport_list.add(row[0])}
                if (row[1] !in station_list) {station_list.add(row[1])}
                //добавить проверку на станцию прибытия, чтобы не пропустить все варианты остановок
            }
        }
        return mapOf("station" to station_list, "transport" to transport_list)
    }
    private fun getList(c: Cursor?): List<String>{

        val l = mutableListOf("")
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
}
