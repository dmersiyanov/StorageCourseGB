package com.mda.storagecoursegb.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mda.storagecoursegb.room.Database.Tables.PASSENGERS

@Entity(tableName = PASSENGERS)
data class Passenger(
    @PrimaryKey
    val passengerName: String,
)
