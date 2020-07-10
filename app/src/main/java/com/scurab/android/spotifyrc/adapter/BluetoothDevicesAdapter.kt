package com.scurab.android.spotifyrc.adapter

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.scurab.android.spotifyrc.R
import com.scurab.android.spotifyrc.databinding.ViewBluetoothDeviceBinding

class BluetoothDevicesAdapter(private val clickListener: (BluetoothDevice) -> Unit) : RecyclerView.Adapter<BluetoothDeviceViewHolder>() {
    private val items = mutableListOf<BluetoothDevice>()

    private val innerClickListener = View.OnClickListener {view ->
        val item = items[(view.layoutParams as RecyclerView.LayoutParams).absoluteAdapterPosition]
        clickListener.invoke(item)
    }
    private lateinit var noName: String

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        noName = recyclerView.context.resources.getString(R.string.no_name)
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BluetoothDeviceViewHolder {
        return BluetoothDeviceViewHolder(ViewBluetoothDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false).also {
            it.root.setOnClickListener(innerClickListener)
        })
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: BluetoothDeviceViewHolder, position: Int) {
        holder.views.deviceName.text = items[position].name ?: noName
        holder.views.deviceAddress.text = items[position].address
    }

    fun setItems(list: List<BluetoothDevice>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

}

class BluetoothDeviceViewHolder(val views: ViewBluetoothDeviceBinding) : RecyclerView.ViewHolder(views.root)