package com.mda.storagecoursegb

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mda.storagecoursegb.databinding.ItemPhotoBinding

class InternalStorageAdapter(
    private val onPhotoClick: (InternalStoragePhoto) -> Unit
) : ListAdapter<InternalStoragePhoto, InternalStorageAdapter.PhotoViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder(
            ItemPhotoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = currentList[position]
        holder.binding.apply {
            ivPhoto.setImageBitmap(photo.bitmap)
            ivPhoto.setOnLongClickListener {
                onPhotoClick(photo)
                true
            }
        }
    }

    inner class PhotoViewHolder(val binding: ItemPhotoBinding) :
        RecyclerView.ViewHolder(binding.root)

    private companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<InternalStoragePhoto>() {
            override fun areItemsTheSame(
                oldItem: InternalStoragePhoto,
                newItem: InternalStoragePhoto
            ): Boolean {
                return oldItem.fileName == newItem.fileName
            }

            override fun areContentsTheSame(
                oldItem: InternalStoragePhoto,
                newItem: InternalStoragePhoto
            ): Boolean {
                return oldItem.fileName == newItem.fileName && oldItem.bitmap.sameAs(newItem.bitmap)
            }
        }
    }
}