package com.example.flash_drop

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.flash_drop.R
import android.widget.ImageView

class ComoUsarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_como_usar)

        // Configurar o botão de voltar
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            // Finaliza a atividade atual e volta para a tela anterior (Configurações)
            finish()
        }
    }

    // Opcional: Também pode lidar com o botão físico de voltar
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}