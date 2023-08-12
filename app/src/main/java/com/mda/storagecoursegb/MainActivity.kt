package com.mda.storagecoursegb

import android.Manifest.permission.READ_MEDIA_IMAGES
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.IntentSender
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import coil.load
import com.mda.storagecoursegb.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val pickMediaLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            showImage(uri)
        }
    private val intentSenderLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            onDeleteResult(it.resultCode)
        }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
                pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSelect.setOnClickListener {
            showImagePicker()

        }
        binding.btnSave.setOnClickListener {
            showImage()
        }

        binding.btnDelete.setOnClickListener {
            selectedImageUri?.let { deleteImage(it) }
        }
    }

    private fun onDeleteResult(resultCode: Int) {
        if (resultCode == RESULT_OK) {
            selectedImageUri = null
            Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Image delete error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteImage(imageUri: Uri) {
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

    private fun showImagePicker() {
        if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(READ_MEDIA_IMAGES)
        } else {
            pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun showImage() {
        binding.ivImage.load("https://i.ibb.co/6gMwbsp/image.jpg")

        // TODO add saving
    }

    private fun getAllImages(): List<Uri> {
        val images = mutableListOf<Uri>()
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
                images.add(contentUri)
            }
        }

        return images
    }

    private fun showImage(uri: Uri?) {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
        )

        if (uri != null) {
            contentResolver.query(
                uri, projection, null, null, "${MediaStore.Images.Media.DISPLAY_NAME} ASC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val displayName = cursor.getString(displayNameColumn)
                    val contentUri =
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    selectedImageUri = contentUri
                    binding.ivImage.setImageURI(selectedImageUri)
                }
            }
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }
}