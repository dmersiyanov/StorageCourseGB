package com.mda.storagecoursegb.room.model

import androidx.room.Entity

@Entity(primaryKeys = ["driverName", "passengerName"])
data class DriverPassengerCrossRef(
    val driverName: String,
    val passengerName: String
)
