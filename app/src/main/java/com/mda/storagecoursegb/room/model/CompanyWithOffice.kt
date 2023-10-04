package com.mda.storagecoursegb.room.model

import androidx.room.Embedded
import androidx.room.Relation

data class CompanyWithOffice(
    @Embedded
    val company: TaxiCompany,
    @Relation(parentColumn = "companyName", entityColumn = "companyName")
    val office: Office
)