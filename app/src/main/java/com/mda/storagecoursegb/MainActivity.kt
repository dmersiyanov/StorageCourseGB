package com.mda.storagecoursegb

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.documentfile.provider.DocumentFile
import com.mda.storagecoursegb.databinding.ActivityMainBinding
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val tag = "MainActivity"
    private val createDocLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri: Uri? ->
            writeFileToSharedStorage(uri)
        }

    private val openDocLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            readFileFromUri(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCreateDoc.setOnClickListener {
            launchCreateFileContract()
        }

        binding.btnOpenDoc.setOnClickListener {
            openDocLauncher.launch(arrayOf("text/plain"))
        }
    }

    private fun launchCreateFileContract() {
        // Объявляем имя файла
        val filename = "my_shared_file"
        createDocLauncher.launch(filename)
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

    private fun readFileFromUri(uri: Uri?) {
        uri?.let {
            val documentFile = DocumentFile.fromSingleUri(this, it)
            if (documentFile?.canRead() == true) {
                contentResolver.openInputStream(documentFile.uri)?.use { ips ->
                    val content = ips.readBytes().toString(Charset.defaultCharset())
                    Log.i(tag, content)
                }
            }
        }
    }
}