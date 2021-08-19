package com.udacity.project4.locationreminders.util

import com.google.android.gms.maps.model.LatLng
import kotlin.math.acos
import kotlin.random.Random

fun generateRandomCoordinates(): LatLng {
    val u: Double = Random.nextDouble()
    val v: Double = Random.nextDouble()

    val latitude = Math.toDegrees(acos(u * 2 - 1)) - 90
    val longitude = 360 * v - 180

    return LatLng(latitude, longitude)
}