package com.medic.app.data

data class Hospital(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

data class HospitalWithBearing(
    val hospital: Hospital,
    val distanceKm: Double,
    val bearingDegrees: Double
)

data class FieldKitItem(
    val name: String,
    val whatItIsFor: String,
    val howToUseSafely: String,
    val warning: String,
    val isMedicine: Boolean
)
