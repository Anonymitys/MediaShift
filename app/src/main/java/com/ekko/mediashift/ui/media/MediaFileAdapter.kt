package com.ekko.mediashift.ui.media

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ekko.mediashift.databinding.ItemMediaFileBinding

class MediaFileAdapter(
    private val onRemove: (SelectedMedia) -> Unit
) : ListAdapter<SelectedMedia, MediaFileAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMediaFileBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemMediaFileBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(media: SelectedMedia) {
            binding.fileName.text = media.name
            val info = buildString {
                if (media.resolutionFormatted.isNotEmpty()) {
                    append(media.resolutionFormatted)
                    append(" · ")
                }
                if (media.durationMs > 0) {
                    append(media.durationFormatted)
                    append(" · ")
                }
                append(media.sizeFormatted)
            }
            binding.fileInfo.text = info
            binding.removeButton.setOnClickListener {
                onRemove(media)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SelectedMedia>() {
        override fun areItemsTheSame(oldItem: SelectedMedia, newItem: SelectedMedia): Boolean =
            oldItem.uri == newItem.uri

        override fun areContentsTheSame(oldItem: SelectedMedia, newItem: SelectedMedia): Boolean =
            oldItem == newItem
    }
}
