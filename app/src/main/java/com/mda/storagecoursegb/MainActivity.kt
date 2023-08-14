package com.mda.storagecoursegb

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.widget.doAfterTextChanged
import com.mda.storagecoursegb.databinding.ActivityMainBinding
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val tag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etFileName.doAfterTextChanged {
            binding.btnSave.isEnabled =
                !it.isNullOrEmpty() && !binding.etFileContent.text.isNullOrEmpty()
        }

        binding.etFileContent.doAfterTextChanged {
            binding.btnSave.isEnabled =
                !it.isNullOrEmpty() && !binding.etFileName.text.isNullOrEmpty()
        }

        binding.btnSave.setOnClickListener {
            writeFileToPrivateStorage(
                binding.etFileName.text.toString(),
                binding.etFileContent.text.toString()
            )
        }
    }

    private fun writeFileToPrivateStorage(name: String, content: String) {
        // создаем файл
        val file = File(this.filesDir, name)

        // запись в файл
        this.openFileOutput(name, Context.MODE_PRIVATE).use {
            it.write(content.toByteArray())
        }

        if (file.exists()) {
            // Вывод путь текущего файла в Logcat
            Log.i(tag, file.path)
        }
    }
}