 package com.example.journey

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*



 class MainActivity : AppCompatActivity(), View.OnClickListener{

    private val REQUEST_PERMISSION_REQUEST_CODE = 34
    val LOG_TAG = "myLogs"

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(LOG_TAG, "-------------------------------------")

    }

     override fun onResume() {
//         во-первых, упростить код использования функции нахождения координат до одной функции
//         во-вторых, сделать вариант приложения без использования координат
         super.onResume()
         val dbh = DBHelper(this)
         val db = dbh.writableDatabase
         val c:Cursor = db.query("station", arrayOf("name, latitude, longitude"), null,
             null, "name",null, null, null)
         val array = getMap(c)
         c.close()
         dbh.close()
         fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
         getCoordinate(array)
         button.setOnClickListener(this)
         button2.setOnClickListener(this)
     }

    override fun onClick(v: View?) {
        when (v?.id){
            R.id.button -> {
                val sSpinnerFrom = spinner_from.selectedItem
                val sSpinnerTo = spinner_to.selectedItem
                val checkedRadioButtonId = radioGroup.checkedRadioButtonId
                val radio = radioGroup.findViewById<RadioButton>(checkedRadioButtonId)
                if (sSpinnerFrom == sSpinnerTo) {
                    Toast.makeText(
                        this,
                        "Пожайлуйста, выберите две разные станции",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val intent = Intent(this, TransportTable::class.java)
                    intent.putExtra("station_from", sSpinnerFrom.toString())
                    intent.putExtra("station_to", sSpinnerTo.toString())
                    intent.putExtra("day_type", radioGroup.indexOfChild(radio).toString())
                    startActivity(intent)
                }
            }
            R.id.button2 -> {
                val intent = Intent(this, SetCoordinate::class.java)
                intent.putExtra("station", spinner_from.selectedItem.toString())
                startActivity(intent)
            }
        }
    }

    fun getCoordinate(map_coordinate: Map<String, List<Double>>)
    {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            requestPermissions()
        }
        else {
            fusedLocationClient.lastLocation!!.addOnSuccessListener  { location: Location? ->
                if (location != null) {
                    val current_location = listOf(location.latitude, location.longitude)
                    val map_distance = mutableMapOf<String, Double>()
                    for ((key, value) in map_coordinate)
                    {
                        val distance = getDistance(value, current_location)
                        map_distance.put(key, distance)
                    }
                    Log.d(LOG_TAG, map_distance.toString())
                    val raw_result = map_distance.toList().sortedBy { (_, value) -> value }
                    Log.d(LOG_TAG, raw_result.toString())
                    val array = mutableListOf<String>()
                    Toast.makeText(this, "Текущая позиция " + current_location[0].toString()
                    + " " + current_location[1].toString(), Toast.LENGTH_SHORT).show()
                    for (pair in raw_result)(
                            array.add(pair.first)
                            )
                    setSpinner(array)

                } else {
                    Log.d(com.example.journey.LOG_TAG, "getLastLocation:exception")
                    Toast.makeText(
                        this, "No location detected. Make Sure location enabled on the diveces",
                        Toast.LENGTH_SHORT
                    ).show()
                    val raw_result = map_coordinate.toList()
                    val array = mutableListOf<String>()
                    for (pair in raw_result)(
                            array.add(pair.first)
                            )
                    setSpinner(array)
                }
            }
        }
    }

     private fun startLocationPermissionRequest(){
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_PERMISSION_REQUEST_CODE
        )
    }
     private fun requestPermissions(){
        Log.i(LOG_TAG, "take permision in main")
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (shouldProvideRationale){
            Log.i(com.example.journey.LOG_TAG, "Displayed permission rationale to provide additional context")
            startLocationPermissionRequest()
        }
        else {
            Log.i(com.example.journey.LOG_TAG, "Requesting permission")
            startLocationPermissionRequest()
        }
    }
     fun setSpinner(array: List<String>){
         val adapter = ArrayAdapter<Any>(this, android.R.layout.simple_spinner_item, array)
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
         fillSpinner(adapter, spinner_from, 0)
         fillSpinner(adapter, spinner_to, 1)
     }
     private fun fillSpinner(adapter: ArrayAdapter<Any>, spinner: Spinner, pos: Int){
         spinner.adapter = adapter
         spinner.setSelection(pos)
     }
}
