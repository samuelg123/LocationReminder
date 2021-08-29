package com.udacity.project4.locationreminders.util

import com.google.android.gms.maps.model.LatLng
import kotlin.math.acos
import kotlin.random.Random

private val places = arrayOf(
    "South Island",
    "New Zealand",
    "Paris",
    "Bora Bora",
    "Maui",
    "Tahiti",
    "London",
    "Rome",
    "Phuket"
)

private val title = arrayOf(
    "Money",
    "Necklace",
    "Bank notes",
    "Coins",
    "Medals",
    "Gold",
    "Keys",
    "Silver",
    "Purse"
)

private const val lorem = "Lorem ipsum dolor sit amet, " +
        "consectetur adipiscing elit, sed do eiusmod tempor "

fun generatePlace(): String {
    val i: Int = places.indices.random()
    return places[i]
}

fun generateDescription(): String = lorem

fun generateTitle(): String {
    val i: Int = title.indices.random()
    return title[i]
}

fun generateRandomCoordinates(): LatLng {
    val u: Double = Random.nextDouble()
    val v: Double = Random.nextDouble()

    val latitude = Math.toDegrees(acos(u * 2 - 1)) - 90
    val longitude = 360 * v - 180

    return LatLng(latitude, longitude)
}