package com.example.journey

import kotlin.math.*

fun getDistance(location: List<Double>, current_location: List<Double>):
        Double{

    val earthRadiusKm = 6371
    val dLat = degreesToRadians(current_location[0]-location[0])
    val dLon = degreesToRadians(current_location[1]-location[1])
    val lat1 = degreesToRadians(location[0])
    val lat2 = degreesToRadians(current_location[0])
    val a = sin(dLat/2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon/2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1-a))

    return c*earthRadiusKm
}

private fun degreesToRadians(degrees: Double): Double{
    return degrees * Math.PI/180
}
