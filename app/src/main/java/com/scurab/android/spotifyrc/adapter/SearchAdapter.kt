package com.scurab.android.spotifyrc.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.scurab.android.spotify.api.model.Item
import com.scurab.android.spotify.api.model.Search
import com.scurab.android.spotifyrc.databinding.ViewAlbumItemBinding

class SearchAdapter(private val clickListener: (Item) -> Unit) : RecyclerView.Adapter<SearchViewHolder>() {

    private var items: List<Item> = emptyList()

    private val innerClickListener = View.OnClickListener { view ->
        val item = items[(view.layoutParams as RecyclerView.LayoutParams).absoluteAdapterPosition]
        clickListener.invoke(item)
    }

    var search: Search? = null
        set(value) {
            field = value
            items = search?.albums?.items?.sortedBy {
                it.name + it.artists.firstOrNull()?.name
            } ?: emptyList()
            notifyDataSetChanged()
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder(ViewAlbumItemBinding.inflate(LayoutInflater.from(parent.context), parent, false).also {
            it.root.setOnClickListener(innerClickListener)
        })
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val item = items[position]
        holder.views.name.text = item.name
        holder.views.artist.text = item.artists?.firstOrNull()?.name + " " + item.type
    }
}

class SearchViewHolder(val views: ViewAlbumItemBinding) : RecyclerView.ViewHolder(views.root)