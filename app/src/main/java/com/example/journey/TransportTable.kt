package com.example.journey

import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_transport_table.*
import java.text.SimpleDateFormat
import java.util.*


class TransportTable : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transport_table)

        val stationFrom = if (intent.getStringExtra("station_from") =="") "%"
                            else intent.getStringExtra("station_from")
        val stationTo = if (intent.getStringExtra("station_to") == "") "%"
                            else intent.getStringExtra("station_to")

        val weekday = checkDay()

        val dbh = DBHelper(this)
        val db = dbh.writableDatabase
        val c: Cursor = db.rawQuery(
            "select t1.transport_id, t1.time_arrive " +
                    "from timetable as t1 " +
                    "inner join route as t2 on t1.start_route_id = t2.id " +
                    "inner join route as t3 on t1.end_route_id = t3.id " +
                    "where t1.day not in ('$weekday') " +
                    "and t2.name like '${stationFrom}' " +
                    "and t3.name like '${stationTo}'",
            null)
        val table = getTable(c)
        val list = searchTime(table)
        val temp = mutableListOf<String>()
        for (key in list.keys){
            temp.add("Маршрут $key отбывает в ${list[key]!![0]}, следующая остановка в ${list[key]!![1]} ")
        }

        val adapter = ArrayAdapter<String>(this, R.layout.my_list_item, temp)
        lvMain.setAdapter(adapter)

        c.close()
        dbh.close()
    }


    private fun checkDay():String{

        val holidays = arrayOf("0101", "0201", "0701", "0803", "0105", "0905", "0306", "0711", "2512")
        val weekdays = arrayOf("6", "7")
        val rawDate = SimpleDateFormat("u ddMM", Locale.getDefault())
        val (weekday, day) = rawDate.format(Date()).split(" ")
        if ((weekday in weekdays)||(day in holidays)) return "workday"
        else return "holiday"
    }


    private fun getTable(c:Cursor?):MutableMap<String, MutableList<String>>{
        val dict = mutableMapOf<String, MutableList<String>>()
        if (c != null){
            if (c.moveToFirst()){
                do{
                    var key:String? = null
                    for (cn in c.columnNames){
                        val value = c.getString(c.getColumnIndex(cn))
                        if (cn == "transport_id"){
                            if(value !in dict){
                                dict[value] = mutableListOf<String>()
                            }
                            key = value
                        }
                        if (cn == "time_arrive"){
                            dict[key]?.add(value)
                        }
                    }
                }while(c.moveToNext())
            }
        }
        else Log.d(LOG_TAG, "Cursor is null")
        for (key in dict.keys) dict[key]?.sort()
        return dict
    }


    private fun searchTime(table: Map<String, List<String>>):
            Map<String, List<String>>{
        val currentTime = SimpleDateFormat("HH:mm").format(Date()).toString()
        val tableList = mutableMapOf<String, List<String>>()
        for (k in table.keys){
            val list = table[k]
            for (i in list!!.indices){
                if (currentTime <= list[i]){
                    if (i < list.size-1) {
                        tableList[k] = listOf(list[i], list[i+1])
                    break
                    }
                    else {
                        tableList[k] = listOf(list[i], list[0])
                        break
                    }
                }
            }
        }
        return tableList
    }
}
