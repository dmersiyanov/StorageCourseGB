package com.mda.storagecoursegb

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.ContentValues
import android.content.IntentSender
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.mda.storagecoursegb.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val readPermission = sdk33AndUp { READ_MEDIA_IMAGES } ?: READ_EXTERNAL_STORAGE
    private var readPermissionGranted = false
    private var writePermissionGranted = false
    private var contentObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
            if (readPermissionGranted) {
                loadImagesFromSharedStorage()
            }
        }
    }

    private val takePrivatePhoto =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { imageBitmap: Bitmap? ->
            lifecycleScope.launch {
                imageBitmap?.let {
                    val isSavedSuccessfully =
                        savePhotoToInternalStorage(UUID.randomUUID().toString(), it)
                    loadImagesFromInternalStorage()
                    onPhotoTakeResult(isSavedSuccessfully)
                }

            }
        }

    private val takeSharedPhoto =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { imageBitmap: Bitmap? ->
            lifecycleScope.launch {
                imageBitmap?.let {
                    val isSavedSuccessfully = if (writePermissionGranted) {
                        savePhotoToExternalStorage(
                            UUID.randomUUID().toString(), it
                        )
                    } else {
                        false
                    }
                    onPhotoTakeResult(isSavedSuccessfully)
                }
            }
        }

    private val intentSenderLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            onDeleteResult(it.resultCode)
        }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions: Map<String, Boolean> ->
            readPermissionGranted = permissions[readPermission] ?: readPermissionGranted
            writePermissionGranted = permissions[WRITE_EXTERNAL_STORAGE] ?: writePermissionGranted

            if (readPermissionGranted) {
                loadImagesFromSharedStorage()
            } else {
                Toast.makeText(this, "Can't read files without permission.", Toast.LENGTH_LONG)
                    .show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvSharedPhotos.adapter = SharedPhotoAdapter {
            lifecycleScope.launch {
                deleteOnSharedStorage(it.contentUri)
            }
        }

        binding.rvInternalPhotos.adapter = InternalStorageAdapter {
            lifecycleScope.launch {
                val success = deleteOnInternalStorage(it.fileName)
                if (success) {
                    loadImagesFromInternalStorage()
                }
            }
        }

        binding.btnTakePrivatePhoto.setOnClickListener {
            takePrivatePhoto.launch()
        }

        binding.btnTakeSharedPhoto.setOnClickListener {
            takeSharedPhoto.launch()
        }

        requestPermissions()
        initContentObserver()
        loadImagesFromSharedStorage()
        loadImagesFromInternalStorage()
    }

    private fun requestPermissions() {
        val hasReadPermission = ContextCompat.checkSelfPermission(
            this,
            readPermission
        ) == PackageManager.PERMISSION_GRANTED

        val hasWritePermission = ContextCompat.checkSelfPermission(
            this,
            WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val minSdk29 = VERSION.SDK_INT >= VERSION_CODES.Q
        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29

        val permissionsToRequest = mutableListOf<String>()
        if (!writePermissionGranted) {
            permissionsToRequest.add(WRITE_EXTERNAL_STORAGE)
        }
        if (!readPermissionGranted) {
            permissionsToRequest.add(readPermission)
        }
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun initContentObserver() {
        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver
        )
    }

    private fun onPhotoTakeResult(isSavedSuccessfully: Boolean) {
        if (isSavedSuccessfully) {
            Toast.makeText(
                this@MainActivity,
                "Photo saved successfully",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this@MainActivity,
                "Failed to save photo",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun onDeleteResult(resultCode: Int) {
        if (resultCode == RESULT_OK) {
            Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Image delete error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadImagesFromSharedStorage() {
        lifecycleScope.launch {
            (binding.rvSharedPhotos.adapter as? SharedPhotoAdapter)?.submitList(getAllSharedImages())
        }
    }

    private fun loadImagesFromInternalStorage() {
        lifecycleScope.launch {
            (binding.rvInternalPhotos.adapter as? InternalStorageAdapter)?.submitList(
                getAllPrivateImages()
            )
        }
    }

    private suspend fun deleteOnInternalStorage(filename: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                deleteFile(filename)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun deleteOnSharedStorage(imageUri: Uri) {
        // Пытаемся удалить картинку с помощью contentResolver
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    contentResolver.delete(imageUri, null, null)
                } catch (e: Exception) {
                    // для API > 29 необходимо явное согласие пользователя,
                    // поэтому нам нужен объект intentSender для запроса разрешения
                    val intentSender: IntentSender? = when {
                        // для API > 30 intentSender получается с помощью метода createDeleteRequest
                        VERSION.SDK_INT >= VERSION_CODES.R -> {
                            MediaStore.createDeleteRequest(
                                contentResolver,
                                listOf(imageUri)
                            ).intentSender
                        }
                        // для API 29 intentSender получается с обработки выброшего исключения с типом RecoverableSecurityException
                        VERSION.SDK_INT >= VERSION_CODES.Q -> {
                            val recoverableSecurityException = e as? RecoverableSecurityException
                            recoverableSecurityException?.userAction?.actionIntent?.intentSender
                        }
                        // Для всех остальных случаев явные разрешения не нужны
                        else -> null
                    }

                    intentSender?.let {
                        intentSenderLauncher.launch(IntentSenderRequest.Builder(it).build())
                    }
                }
            }
        }
    }

    private suspend fun savePhotoToExternalStorage(displayName: String, bmp: Bitmap): Boolean {
        return withContext(Dispatchers.IO) {
            val imageCollection = sdk29AndUp {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.WIDTH, bmp.width)
                put(MediaStore.Images.Media.HEIGHT, bmp.height)
            }
            try {
                contentResolver.insert(imageCollection, contentValues)?.also { uri ->
                    contentResolver.openOutputStream(uri).use { outputStream ->
                        if (!bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
                            throw IOException("Couldn't save bitmap")
                        }
                    }
                } ?: throw IOException("Couldn't create MediaStore entry")
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }
    }

    private suspend fun savePhotoToInternalStorage(filename: String, bmp: Bitmap): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                openFileOutput("$filename.jpg", MODE_PRIVATE).use { stream ->
                    if (!bmp.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                        throw IOException("Couldn't save bitmap.")
                    }
                }
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }
    }

    private suspend fun getAllPrivateImages(): List<InternalStoragePhoto> {
        return withContext(Dispatchers.IO) {
            val files = filesDir.listFiles()
            files?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }?.map {
                val bytes = it.readBytes()
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                InternalStoragePhoto(it.name, bmp)
            } ?: listOf()
        }
    }

    private fun getAllSharedImages(): List<SharedStoragePhoto> {
        val images = mutableListOf<SharedStoragePhoto>()
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        val collection =
            sdk29AndUp { MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY) }
                ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val cursor = contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use {
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                images.add(SharedStoragePhoto(id, contentUri))
            }
        }

        return images
    }
}