package com.example.proyecto_final

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class PongActivity : AppCompatActivity() {

    private lateinit var pongView: PongView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crear la vista del juego Pong
        pongView = PongView(this, null)
        setContentView(pongView)
    }

    override fun onPause() {
        super.onPause()
        // El juego se pausa automáticamente cuando se destruye la superficie
    }

    override fun onResume() {
        super.onResume()
        // El juego se reanuda automáticamente cuando se crea la superficie
    }
}
