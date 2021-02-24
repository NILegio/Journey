package com.example.journey

import android.content.ContentValues
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.Manifest
import android.content.pm.PackageManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_set_coordinate.*
import java.lang.NumberFormatException

class SetCoordinate : AppCompatActivity(), View.OnClickListener{

    private val REQUEST_PERMISSION_REQUEST_CODE = 34
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_coordinate)

        Log.d(LOG_TAG, "startActivity")
        val dbh = DBHelper(this)
        val db = dbh.writableDatabase
        val columns = arrayOf("latitude", "longitude")
        val selection = "name = ?"
        val station = arrayOf(intent.getStringExtra("station"))

        val c = db.query("station", columns, selection, station,
            null, null, null)
        val arrays = getList(c)

        textStation.text = station[0]
        etLatitude.hint = arrays[0].toString()
        etLongitude.hint = arrays[1].toString()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        c.close()
        dbh.close()

        btnauto.setOnClickListener(this)
        btnmanually.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        val station = arrayOf(textStation.text.toString())
        when (v?.id){
            R.id.btnmanually->{

//                val latitude = if (etLatitude.text.toString() == "") etLatitude.hint.toString()
//                    else etLatitude.text
//                val longitude = if (etLongitude.text.toString() == "") etLongitude.hint.toString()
//                    else etLongitude.text
//                проверка вносимых данных должна работать корректнее чем сейчас.
//                ну и обновление данных должно быть сразу видно в блоке EditText
                val latitude = checkText(etLatitude)
                val longitude = checkText(etLongitude)

                setCoordinate(latitude, longitude, station)
            }
            R.id.btnauto ->{
                getCoordinate(station)
            }
        }
    }

    fun getCoordinate(station: Array<String>)
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
                        val latitude = location.latitude
                        val longitude = location.longitude
                        setCoordinate(latitude.toString(), longitude.toString(), station)
                    } else {
                        Log.d(LOG_TAG, "getLastLocation:exception")
                        Toast.makeText(
                            this, "No location detected. Make Sure location enabled on the diveces",
                            Toast.LENGTH_SHORT
                        ).show()
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
        Log.i(LOG_TAG, "take permision in setcoordiate")
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (shouldProvideRationale){
            Log.i(LOG_TAG, "Displayed permission rationale to provide additional context")
                startLocationPermissionRequest()
        }
        else {
            Log.i(LOG_TAG, "Requesting permission")
            startLocationPermissionRequest()
        }
    }

    fun setCoordinate(latitude:Any, longitude: Any,
                      station:Array<String>){
        val dbh = DBHelper(this)
        val db = dbh.writableDatabase
        val cv = ContentValues()
        cv.put("latitude",  latitude.toString())
        cv.put("longitude", longitude.toString())

        db.update("station", cv, "name = ?", station)
        dbh.close()

        Toast.makeText(this, "Set coordinate" + latitude.toString() +
        " " + longitude.toString(), Toast.LENGTH_LONG).show()
    }

    fun checkText(coordinate: EditText):String{
        when(coordinate.text.toString()){
            "" -> {return coordinate.hint.toString()}
            else -> {
                val temp = coordinate.text.toString()
                try{
                    temp.toDouble()
                    return temp
                }catch(e: NumberFormatException){
                    Toast.makeText(this, "Пожайлуйста введите число", Toast.LENGTH_LONG).show()
                    return coordinate.hint.toString()
                }
            }
        }
    }
}