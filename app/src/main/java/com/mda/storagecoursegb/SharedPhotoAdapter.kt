package com.mda.storagecoursegb

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mda.storagecoursegb.databinding.ItemPhotoBinding

class SharedPhotoAdapter(
    private val onPhotoClick: (SharedStoragePhoto) -> Unit
) : ListAdapter<SharedStoragePhoto, SharedPhotoAdapter.PhotoViewHolder>(DIFF_CALLBACK) {

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
            ivPhoto.setImageURI(photo.contentUri)
            ivPhoto.setOnLongClickListener {
                onPhotoClick(photo)
                true
            }
        }
    }

    inner class PhotoViewHolder(val binding: ItemPhotoBinding) :
        RecyclerView.ViewHolder(binding.root)

    private companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SharedStoragePhoto>() {
            override fun areItemsTheSame(
                oldItem: SharedStoragePhoto,
                newItem: SharedStoragePhoto
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: SharedStoragePhoto,
                newItem: SharedStoragePhoto
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}