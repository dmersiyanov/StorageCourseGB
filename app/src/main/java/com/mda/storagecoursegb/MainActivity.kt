package com.mda.storagecoursegb

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.mda.storagecoursegb.databinding.ActivityMainBinding
import com.mda.storagecoursegb.room.Database
import com.mda.storagecoursegb.room.TaxiCompanyDao
import com.mda.storagecoursegb.room.model.Driver
import com.mda.storagecoursegb.room.model.DriverPassengerCrossRef
import com.mda.storagecoursegb.room.model.Office
import com.mda.storagecoursegb.room.model.Passenger
import com.mda.storagecoursegb.room.model.TaxiCompany
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDb()
    }

    private fun initDb() {
        val db = Database.getDb(applicationContext)
        val dao = db.taxiCompanyDao()

        addTestData(dao)
    }

    private fun addTestData(dao: TaxiCompanyDao) {
        lifecycleScope.launch(Dispatchers.IO) {
            dao.insertDriver(Driver("Alex", "Company_1"))
            dao.insertDriver(Driver("Bob", "Company_2"))
            dao.insertDriver(Driver("Mike", "Company_2"))

            dao.insertCompany(TaxiCompany("Company_1"))
            dao.insertCompany(TaxiCompany("Company_2"))

            dao.insertOffice(Office("New York", "Company_1"))
            dao.insertOffice(Office("London", "Company_2"))

            dao.insertPassenger(Passenger("James"))
            dao.insertPassenger(Passenger("Robert"))
            dao.insertPassenger(Passenger("John"))

            dao.insertDriverPassengerCrossRef(DriverPassengerCrossRef("Alex", "James"))
            dao.insertDriverPassengerCrossRef(DriverPassengerCrossRef("Alex", "Robert"))
            dao.insertDriverPassengerCrossRef(DriverPassengerCrossRef("Bob", "John"))

            val taxiCompanyWithDrivers = dao.getTaxiCompanyWithDrivers()
            Log.i("tag", taxiCompanyWithDrivers.toString())


            val companyWithOfficeList = dao.getTaxiCompanyWithOffices()
            Log.i("tag", companyWithOfficeList.toString())

            val driverWithPassengers = dao.getDriverWithPassengers()
            Log.i("tag", driverWithPassengers.toString())
        }
    }
}