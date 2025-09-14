package com.example.proyecto_final

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto_final.utils.SessionManager

// Actividad principal que muestra información del usuario y permite cerrar sesión

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)

        // Verificar si el usuario está logueado
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupUserInfo()
        setupLogoutButton()

        // Configurar el botón para ir al juego Snake
        val btnSnake = findViewById<Button>(R.id.btnSnake)
        btnSnake.setOnClickListener {
            val intent = Intent(this, com.example.proyecto_final.juegos.Snake::class.java)
            startActivity(intent)
        }
    }

    // Configurar la información del usuario en la interfaz
    private fun setupUserInfo() {
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val userName = sessionManager.getUserName()
        tvWelcome.text = "¡Bienvenido ${userName ?: "Usuario"}!"
    }

    // Configurar el botón de cerrar sesión
    private fun setupLogoutButton() {
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            // Cerrar sesión y limpiar datos
            sessionManager.logout()

            // Volver al login y limpiar el stack de actividades
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}