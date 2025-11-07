package com.example.flash_drop

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.flash_drop.R

class HelpActivity : AppCompatActivity() {

    @SuppressLint("WrongViewCast", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        // Botão de voltar para SettingsActivity
        val btnVoltar = findViewById<ImageView>(R.id.btnVoltar)
        btnVoltar.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            finish() // fecha a tela de ajuda
        }

        // Botão "Entrar em Contato"
        val btnContato = findViewById<Button>(R.id.btnContato)
        btnContato.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("suporte@flashdrop.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Ajuda - FlashDrop")
                putExtra(Intent.EXTRA_TEXT, "Olá! Preciso de ajuda com o aplicativo FlashDrop.")
            }

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }
    }
}
