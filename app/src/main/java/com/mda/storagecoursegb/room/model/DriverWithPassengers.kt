package com.mda.storagecoursegb.room.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class DriverWithPassengers(
    @Embedded val driver: Driver,
    @Relation(
        parentColumn = "driverName",
        entityColumn = "passengerName",
        associateBy = Junction(DriverPassengerCrossRef::class)
    )
    val passengers: List<Passenger>
)
