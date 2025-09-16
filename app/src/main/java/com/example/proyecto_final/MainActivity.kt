package com.example.proyecto_final

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto_final.Juegos.MemoramaActivity
import com.example.proyecto_final.Juegos.Snake
import com.example.proyecto_final.utils.SessionManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView

// Actividad principal que muestra información del usuario y permite cerrar sesión

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var exoPlayerMemorama: ExoPlayer

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

        // Configurar ExoPlayer para el video de Snake
        val playerView = findViewById<PlayerView>(R.id.videoButton)
        exoPlayer = ExoPlayer.Builder(this).build()
        playerView.player = exoPlayer
        val rawUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.prueba_para_juego)
        val mediaItem = MediaItem.fromUri(rawUri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        val btnPlaySnake = findViewById<Button>(R.id.btnPlaySnake)
        playerView.setOnClickListener {
            if (!exoPlayer.isPlaying) {
                exoPlayer.play()
            }
            btnPlaySnake.visibility = View.VISIBLE
        }
        btnPlaySnake.setOnClickListener {
            val intent = Intent(this, Snake::class.java)
            startActivity(intent)
        }

        // Configurar segundo ExoPlayer para el video de Memorama
        val playerViewMemorama = findViewById<PlayerView>(R.id.videoButtonMemorama)
        exoPlayerMemorama = ExoPlayer.Builder(this).build()
        playerViewMemorama.player = exoPlayerMemorama
        val rawUriMemorama = Uri.parse("android.resource://" + packageName + "/" + R.raw.prueba_para_juego)
        val mediaItemMemorama = MediaItem.fromUri(rawUriMemorama)
        exoPlayerMemorama.setMediaItem(mediaItemMemorama)
        exoPlayerMemorama.prepare()
        val btnPlayMemorama = findViewById<Button>(R.id.btnPlayMemorama)
        playerViewMemorama.setOnClickListener {
            if (!exoPlayerMemorama.isPlaying) {
                exoPlayerMemorama.play()
            }
            btnPlayMemorama.visibility = View.VISIBLE
        }
        btnPlayMemorama.setOnClickListener {
            val intent = Intent(this, MemoramaActivity::class.java)
            startActivity(intent)
        }
    }

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

    override fun onDestroy() {
        super.onDestroy()
        if (::exoPlayer.isInitialized) {
            exoPlayer.release()
        }
        if (::exoPlayerMemorama.isInitialized) {
            exoPlayerMemorama.release()
        }
    }
}
