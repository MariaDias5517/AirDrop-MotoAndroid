package com.example.flash_drop.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flash_drop.R
import com.example.flash_drop.data.devices.Device

class DevicesAdapter (
    private val devices: List<Device>,
    private val onItemClick: (Device) -> Unit
) : RecyclerView.Adapter<DevicesAdapter.DeviceViewHolder>() {

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgDevice: ImageView = itemView.findViewById(R.id.imgDevice)
        private val tvDeviceName: TextView = itemView.findViewById(R.id.tvDeviceName)
        private val tvDeviceStatus: TextView = itemView.findViewById(R.id.tvDeviceStatus)

        fun bind(device: Device) {
            tvDeviceName.text = device.name
            tvDeviceStatus.text = device.status
            imgDevice.setImageResource(device.iconRes)

            itemView.setOnClickListener {
                onItemClick(device) // Passa o device pro MainActivity
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount(): Int = devices.size
}
