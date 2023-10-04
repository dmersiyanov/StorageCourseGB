package com.mda.storagecoursegb.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mda.storagecoursegb.room.Database.Tables.DRIVERS

@Entity(tableName = DRIVERS)
data class Driver(
    @PrimaryKey
    val driverName: String,
    val companyName: String
)
