import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flash_drop.R
import com.example.flash_drop.data.devices.Device

class DevicesAdapter(
    private val devices: List<Device>
) : RecyclerView.Adapter<DevicesAdapter.DeviceViewHolder>() {

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgDevice: ImageView = itemView.findViewById(R.id.imgDevice)
        val tvName: TextView = itemView.findViewById(R.id.tvDeviceName)
        val tvStatus: TextView = itemView.findViewById(R.id.tvDeviceStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]
        holder.imgDevice.setImageResource(device.iconRes)
        holder.tvName.text = device.name
        holder.tvStatus.text = device.status
    }

    override fun getItemCount() = devices.size
}
