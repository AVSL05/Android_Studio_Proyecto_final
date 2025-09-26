package com.example.proyecto_final

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Dificultades_2048 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dificultades_2048)

        val btn4x4: Button = findViewById(R.id.btn4x4)
        val btn5x5: Button = findViewById(R.id.btn5x5)
        val btn6x6: Button = findViewById(R.id.btn6x6)

        btn4x4.setOnClickListener { iniciarJuego(4) }
        btn5x5.setOnClickListener { iniciarJuego(5) }
        btn6x6.setOnClickListener { iniciarJuego(6) }
    }

    private fun iniciarJuego(tamano: Int) {
        val intent = Intent(this, Tablero_2048::class.java)
        intent.putExtra("TAMANO", tamano)
        startActivity(intent)
    }
}