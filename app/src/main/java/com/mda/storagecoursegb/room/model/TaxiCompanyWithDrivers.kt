package com.mda.storagecoursegb.room.model

import androidx.room.Embedded
import androidx.room.Relation

data class TaxiCompanyWithDrivers(
    @Embedded val company: TaxiCompany,
    @Relation(
        parentColumn = "companyName",
        entityColumn = "companyName"
    )
    val drivers: List<Driver>
)