package com.example.journey

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), View.OnClickListener{

    private val LOG_TAG = "myLogs"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(LOG_TAG, "-------------------------------------")
        val dbh = DBHelper(this)
        val db = dbh.writableDatabase
        val c:Cursor = db.rawQuery("select name from station", null)
        val array = getList(c)
        c.close()
        dbh.close()

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, array)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        fillSpinner(adapter, spinner_from, 0)
        fillSpinner(adapter, spinner_to, 1)
        button.setOnClickListener(this)
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

    private fun fillSpinner(adapter: ArrayAdapter<String>, spinner: Spinner, pos: Int){
        spinner.adapter = adapter
        spinner.setSelection(pos)
    }

    override fun onClick(v: View?) {
        val sSpinnerFrom = spinner_from.selectedItem
        val sSpinnerTo = spinner_to.selectedItem
        val checkedRadioButtonId = radioGroup.checkedRadioButtonId
        val radio = radioGroup.findViewById<RadioButton>(checkedRadioButtonId)
        if (sSpinnerFrom == sSpinnerTo){
            Toast.makeText(this, "Пожайлуйста, выберите две разные станции", Toast.LENGTH_SHORT).show()
        }
        else{
            val intent = Intent(this, TransportTable::class.java)
            intent.putExtra("station_from", sSpinnerFrom.toString())
            intent.putExtra("station_to", sSpinnerTo.toString())
            intent.putExtra("day_type", radioGroup.indexOfChild(radio).toString())
            startActivity(intent)
        }
    }
}