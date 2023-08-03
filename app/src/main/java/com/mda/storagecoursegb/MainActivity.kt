package com.mda.storagecoursegb

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.mda.storagecoursegb.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val tag = "MainActivity"
    private val createDocLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri: Uri? ->
            writeFileToSharedStorage(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startCreateFileContract()

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun startCreateFileContract() {
        // Объявляем имя файла
        val filename = "my_shared_file"
        createDocLauncher.launch(filename)
    }

    private fun writeFileToPrivateStorage() {
        // создаем файл
        val filename = "myfile"

        val file = File(this.filesDir, filename)

        // запись в файл
        val fileContents = "Hello world!"
        this.openFileOutput(filename, Context.MODE_PRIVATE).use {
            it.write(fileContents.toByteArray())
        }

        Log.i(tag, file.path)
    }


    private fun writeFileToSharedStorage(uri: Uri?) {
        // Объявляем содержимое файла
        val fileContents = "Hello world!"

        // запись в файл с помощью URI и contentResolver
        uri?.let {
            contentResolver.openOutputStream(it)?.use { os ->
                os.write(fileContents.toByteArray())
            }
            Log.i(tag, uri.toString())
        }
    }
}