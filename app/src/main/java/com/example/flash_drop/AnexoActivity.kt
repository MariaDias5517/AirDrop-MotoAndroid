package com.example.flash_drop

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class AnexoActivity : AppCompatActivity() {

    private lateinit var filesContainer: LinearLayout

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val fileName = getFileName(uri)
            addFileToList(fileName)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.anexo_arquivos)

        val btnHome = findViewById<ImageButton>(R.id.btnHome)

        btnHome.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_rigth)
        }

        val deviceName = intent.getStringExtra("deviceName")
        Toast.makeText(this, "Anexando arquivo para: $deviceName", Toast.LENGTH_LONG).show()

        // Inicializa container de arquivos
        filesContainer = findViewById(R.id.filesContainer)

        // REMOVA ESTAS LINHA QUE CAUSAM O ERRO:
        // ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
        //     val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        //     v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
        //     insets
        // }

        // CONFIGURAÇÃO DO SPINNER
        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view as TextView
                textView.setTextColor(if (position == 0) Color.GRAY else Color.BLACK)
                return view
            }
        }

        // Botão de Anexar
        val btnAnexar = findViewById<Button>(R.id.btnAnexar)
        btnAnexar.setOnClickListener {
            pickFileLauncher.launch("*/*") // Aceita qualquer tipo de arquivo
        }
    }

    private fun getFileName(uri: Uri): String {
        var name = "Arquivo"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    private fun addFileToList(fileName: String) {
        val textView = TextView(this).apply {
            text = fileName
            textSize = 16f
            setTextColor(Color.BLACK)
            setPadding(8, 8, 8, 8)
        }

        filesContainer.addView(textView)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_rigth)
    }
}