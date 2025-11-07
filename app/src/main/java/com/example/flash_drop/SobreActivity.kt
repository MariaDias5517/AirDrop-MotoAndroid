package com.example.flash_drop

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SobreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // Botão voltar (sobre o header)
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right)
        }

        // Ações
        val btnCompartilhar = findViewById<LinearLayout>(R.id.btnCompartilhar)
        val btnContato = findViewById<LinearLayout>(R.id.btnContato)
        val btnSite = findViewById<LinearLayout>(R.id.btnSite)

        btnCompartilhar.setOnClickListener {
            // Compartilhar texto simples (link ou mensagem)
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "FlashDrop")
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Teste o FlashDrop — transfira arquivos rápido e sem internet!")
            startActivity(Intent.createChooser(shareIntent, "Compartilhar via"))
        }

        btnContato.setOnClickListener {
            // Abrir app de e-mail
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:") // somente apps de email
                putExtra(Intent.EXTRA_EMAIL, arrayOf("suporte@flashdrop.app"))
                putExtra(Intent.EXTRA_SUBJECT, "Suporte FlashDrop")
                putExtra(Intent.EXTRA_TEXT, "Olá, preciso de ajuda com...")
            }
            try {
                startActivity(emailIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "Nenhum app de e-mail instalado", Toast.LENGTH_SHORT).show()
            }
        }

        btnSite.setOnClickListener {
            // Abrir site
            val url = "https://www.flashdrop.example"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right)
    }
}

