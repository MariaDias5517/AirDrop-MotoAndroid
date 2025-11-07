package com.example.flash_drop

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Transição suave ao abrir (mantido)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        // Buscando os botões de navegação (BOTTOM BAR)
        // **IMPORTANTE:** Se o erro persistir, verifique SEU activity_settings.xml!
        val btnHistorico = findViewById<ImageButton>(R.id.btnHistoricoImg)
        val btnHome = findViewById<ImageButton>(R.id.btnHome)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings) // Necessário para ter a referência, mesmo que não faça nada.

        // Buscando as opções da tela de Configurações
        val opcaoSalvar = findViewById<LinearLayout>(R.id.opcaoSalvar)
        val opcaoCache = findViewById<LinearLayout>(R.id.opcaoCache)
        val opcaoSobre = findViewById<LinearLayout>(R.id.opcaoSobre)
        val opcaoComoUsar = findViewById<LinearLayout>(R.id.opcaoComoUsar)
        val opcaoTema = findViewById<LinearLayout>(R.id.opcaoTema)
        val switchLogout = findViewById<Switch>(R.id.switchLogout)

        // 🚀 Lógica de Navegação da Barra Inferior

        // Histórico
        btnHistorico.setOnClickListener {
            val intent = Intent(this, HistoricoActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish() // Garante que a pilha fique limpa ao sair da tela
        }

        // Home
        btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish() // Garante que a pilha fique limpa ao sair da tela
        }

        // Configurações (já estamos aqui)
        // btnSettings.setOnClickListener { /* Sem ação, mas pode adicionar destaque visual */ }

        // Efeito de clique: feedback visual + animação leve
        fun animarClique(view: LinearLayout) {
            view.alpha = 0.6f
            view.animate().alpha(1f).setDuration(150).start()
        }

        // Lógica das Opções de Configuração (mantida)
        opcaoSalvar.setOnClickListener {
            animarClique(it as LinearLayout)
            Toast.makeText(this, "Fotos serão salvas no rolo de câmera", Toast.LENGTH_SHORT).show()
        }

        opcaoCache.setOnClickListener {
            animarClique(it as LinearLayout)
            Toast.makeText(this, "Cache limpo com sucesso!", Toast.LENGTH_SHORT).show()
        }

        opcaoSobre.setOnClickListener {
            animarClique(it as LinearLayout)
            startActivity(Intent(this, SobreActivity::class.java))
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        opcaoComoUsar.setOnClickListener {
            animarClique(it as LinearLayout)
            startActivity(Intent(this, ComoUsarActivity::class.java))
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        opcaoTema.setOnClickListener {
            animarClique(it as LinearLayout)
            Toast.makeText(this, "Alternar entre modo claro e escuro", Toast.LENGTH_SHORT).show()
        }

        switchLogout.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "Você saiu da conta", Toast.LENGTH_SHORT).show()
                switchLogout.isChecked = false
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}