package com.scurab.android.spotifyrc.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.scurab.android.spotifyrc.databinding.ViewTrackItemBinding
import com.scurab.android.spotifyrc.model.STrack

class AlbumTracksAdapter(private val clickListener: (STrack) -> Unit) : RecyclerView.Adapter<AlbumTrackViewHolder>() {
    var playginUri: String? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var items: List<STrack> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val innerClickListener = View.OnClickListener { view ->
        val item = items[(view.layoutParams as RecyclerView.LayoutParams).absoluteAdapterPosition]
        clickListener.invoke(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumTrackViewHolder {
        return AlbumTrackViewHolder(ViewTrackItemBinding.inflate(LayoutInflater.from(parent.context), parent, false).also {
            it.root.setOnClickListener(innerClickListener)
        })
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: AlbumTrackViewHolder, position: Int) {
        val item = items[position]
        holder.views.name.text = item.name
        holder.views.root.isSelected = item.uri == playginUri
    }
}


class AlbumTrackViewHolder(val views: ViewTrackItemBinding) : RecyclerView.ViewHolder(views.root)