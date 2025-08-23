package com.example.flash_drop

import DevicesAdapter
import FilesAdapter
import android.os.Bundle
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
            Device("Moto G", "Disponível", R.drawable.ic_phone_android),
            Device("Legion", "Disponível", R.drawable.ic_phone_android),
            Device("Samsung A12", "Disponível", R.drawable.ic_phone_android),
            Device("Moto G", "Disponível", R.drawable.ic_phone_android),
            Device("Legion", "Disponível", R.drawable.ic_phone_android),
            Device("Moto G", "Disponível", R.drawable.ic_phone_android),
            Device("Legion", "Disponível", R.drawable.ic_phone_android),
            Device("Moto G", "Disponível", R.drawable.ic_phone_android),
            Device("Legion", "Disponível", R.drawable.ic_phone_android),
            Device("Moto G", "Disponível", R.drawable.ic_phone_android),
            Device("Legion", "Disponível", R.drawable.ic_phone_android)
        )

        val files = listOf(
            FileItem("Documentação Banner.docx", "85.7 MB", R.drawable.ic_insert_drive_file),
            FileItem("Foto01.jpg", "Enviado para: Moto E20", R.drawable.ic_image)
        )

        devicesAdapter = DevicesAdapter(devices)
        filesAdapter = FilesAdapter(files)

        rvDevices.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvDevices.adapter = devicesAdapter

        rvFiles.layoutManager = LinearLayoutManager(this)
        rvFiles.adapter = filesAdapter
    }
}