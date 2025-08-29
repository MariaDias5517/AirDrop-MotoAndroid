package com.example.flash_drop

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class HistoricoActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.historico)

        val btnHome = findViewById<ImageButton>(R.id.btnHome)

        btnHome.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}