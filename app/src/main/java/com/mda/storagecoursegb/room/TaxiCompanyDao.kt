package com.mda.storagecoursegb.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mda.storagecoursegb.room.model.CompanyWithOffice
import com.mda.storagecoursegb.room.model.Driver
import com.mda.storagecoursegb.room.model.DriverPassengerCrossRef
import com.mda.storagecoursegb.room.model.DriverWithPassengers
import com.mda.storagecoursegb.room.model.Office
import com.mda.storagecoursegb.room.model.Passenger
import com.mda.storagecoursegb.room.model.TaxiCompany
import com.mda.storagecoursegb.room.model.TaxiCompanyWithDrivers

@Dao
interface TaxiCompanyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriver(driver: Driver)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriverPassengerCrossRef(crossRef: DriverPassengerCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffice(office: Office)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassenger(passenger: Passenger)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompany(taxiCompany: TaxiCompany)

    @Transaction
    @Query("SELECT * FROM taxi_companies")
    fun getTaxiCompanyWithDrivers(): List<TaxiCompanyWithDrivers>

    @Transaction
    @Query("SELECT * FROM taxi_companies")
    fun getTaxiCompanyWithOffices(): List<CompanyWithOffice>

    @Transaction
    @Query("SELECT * FROM drivers")
    fun getDriverWithPassengers(): List<DriverWithPassengers>
}