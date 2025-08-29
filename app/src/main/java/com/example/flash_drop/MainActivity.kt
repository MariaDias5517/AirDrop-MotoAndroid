package com.example.flash_drop

import DevicesAdapter
import FilesAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flash_drop.data.devices.FileItem
import com.example.flash_drop.data.devices.Device

class MainActivity : AppCompatActivity() {
    private lateinit var devicesAdapter: DevicesAdapter
    private lateinit var filesAdapter: FilesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rvDevices = findViewById<RecyclerView>(R.id.rvDevices)
        val rvFiles = findViewById<RecyclerView>(R.id.rvFiles)

        val devices = listOf(
            Device("Moto G", "Disponível", R.drawable.mobile),
            Device("Legion", "Disponível", R.drawable.laptop),
            Device("Samsung A12", "Disponível", R.drawable.mobile),
            Device("Galaxy S24 Ultra", "Disponível", R.drawable.mobile),
            Device("Windows", "Disponível", R.drawable.laptop),
            Device("Moto Edge", "Disponível", R.drawable.mobile),
            Device("Legion", "Disponível", R.drawable.laptop),
            Device("Galaxy A15", "Disponível", R.drawable.mobile),
            Device("Redmi 13C", "Disponível", R.drawable.mobile),
            Device("Legion", "Disponível", R.drawable.laptop),
        )

        val files = listOf(
            FileItem("Documentação Banner.docx", "85.7 MB", R.drawable.ic_insert_drive_file),
            FileItem("Foto01.jpg", "Enviado para: Moto E20", R.drawable.ic_image),
            FileItem("Foto02.jpg", "Enviado para: Moto E20", R.drawable.ic_image)
        )

        // 👉 Adapter de Devices (clique abre a AnexoActivity)
        devicesAdapter = DevicesAdapter(devices) { device ->
            val intent = Intent(this, AnexoActivity::class.java)
            intent.putExtra("deviceName", (device as Device).name)
            startActivity(intent)
        }


        // 👉 Adapter de Files
        filesAdapter = FilesAdapter(files)

        // Configuração RecyclerView
        rvDevices.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvDevices.adapter = devicesAdapter

        rvFiles.layoutManager = LinearLayoutManager(this)
        rvFiles.adapter = filesAdapter

        // Botões de histórico
        val btnHistorico = findViewById<Button>(R.id.btnHistorico)
        val btnHistoricoImg = findViewById<ImageButton>(R.id.btnHistoricoImg)

        btnHistorico.setOnClickListener {
            startActivity(Intent(this, HistoricoActivity::class.java))
        }

        btnHistoricoImg.setOnClickListener {
            startActivity(Intent(this, HistoricoActivity::class.java))
        }
    }
}
