package com.mda.storagecoursegb.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mda.storagecoursegb.room.Database.Tables.TAXI_COMPANIES

@Entity(tableName = TAXI_COMPANIES)
data class TaxiCompany(
    @PrimaryKey
    val companyName: String
)
