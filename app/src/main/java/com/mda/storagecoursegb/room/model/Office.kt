package com.mda.storagecoursegb.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mda.storagecoursegb.room.Database.Tables.OFFICES

@Entity(tableName = OFFICES)
data class Office(
    @PrimaryKey
    val address: String,
    val companyName: String,
)
