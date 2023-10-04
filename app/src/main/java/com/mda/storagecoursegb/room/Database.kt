package com.mda.storagecoursegb.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mda.storagecoursegb.room.model.Driver
import com.mda.storagecoursegb.room.model.Passenger
import com.mda.storagecoursegb.room.model.TaxiCompany


@Database(
    entities = [TaxiCompany::class, Driver::class, Passenger::class],
    version = 1,
    exportSchema = true
)
abstract class MyAppDatabase : RoomDatabase() {
    abstract fun taxiCompanyDao(): TaxiCompanyDao
}

object Database {

    @Volatile
    private var INSTANCE: MyAppDatabase? = null

    object Tables {
        const val TAXI_COMPANIES = "taxi_companies"
        const val DRIVERS = "drivers"
        const val PASSENGERS = "passengers"
    }

    fun getDb(context: Context): MyAppDatabase = INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
            context,
            MyAppDatabase::class.java,
            "DB_NAME"
        ).build()
        INSTANCE = instance
        instance
    }
}