package com.example.proyecto_final

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.proyecto_final.Juegos.Buscaminas
import com.example.proyecto_final.Juegos.MemoramaActivity
import com.example.proyecto_final.Juegos.Snake
import com.example.proyecto_final.Juegos.SudokuActivity
import com.example.proyecto_final.utils.SessionManager
import android.widget.Toast

// Actividad principal que muestra información del usuario y permite cerrar sesión

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    // exoPlayerBuscaMinas removed; usamos GIF for BuscaMinas

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

        // Cargar GIFs para Snake y Memorama (ImageView)
        val imageSnake = findViewById<ImageView>(R.id.imageSnake)
        val snakeUri = Uri.parse("android.resource://" + packageName + "/raw/snake")
        Glide.with(this).asGif().load(snakeUri).into(imageSnake)
        val btnPlaySnake = findViewById<Button>(R.id.btnPlaySnake)
        imageSnake.setOnClickListener { btnPlaySnake.visibility = View.VISIBLE }
        btnPlaySnake.setOnClickListener {
            val intent = Intent(this, Snake::class.java)
            startActivity(intent)
        }

        val imageMemorama = findViewById<ImageView>(R.id.imageMemorama)
        val memoramaUri = Uri.parse("android.resource://" + packageName + "/raw/memorama")
        Glide.with(this).asGif().load(memoramaUri).into(imageMemorama)
        val btnPlayMemorama = findViewById<Button>(R.id.btnPlayMemorama)
        imageMemorama.setOnClickListener { btnPlayMemorama.visibility = View.VISIBLE }
        btnPlayMemorama.setOnClickListener {
            val intent = Intent(this, MemoramaActivity::class.java)
            startActivity(intent)
        }

        // Usar GIF para Sudoku: cargar en ImageView y mostrar botón al click
        val imageSudoku = findViewById<ImageView>(R.id.imageSudoku)
        // asegurar visible
        imageSudoku.visibility = View.VISIBLE
        // cargar vía Uri (más fiable en algunos dispositivos)
        val sudokuUri = Uri.parse("android.resource://" + packageName + "/raw/sudoku")
        Glide.with(this).asGif().load(sudokuUri).into(imageSudoku)
        val btnPlaySudoku = findViewById<Button>(R.id.btnPlaySudoku)
        imageSudoku.setOnClickListener {
            btnPlaySudoku.visibility = View.VISIBLE
        }
        btnPlaySudoku.setOnClickListener {
            val intent = Intent(this, SudokuActivity::class.java)
            startActivity(intent)
        }

        // Cargar GIF para BuscaMinas y abrir la actividad
        val imageBuscaMinas = findViewById<ImageView>(R.id.imageBuscaMinas)
        // se renombró el gif a buscaminas_anim.gif para evitar conflicto con buscaminas.mp3
        val buscaminasUri = Uri.parse("android.resource://" + packageName + "/raw/buscaminas_anim")
        Glide.with(this).asGif().load(buscaminasUri).into(imageBuscaMinas)
        val btnPlayBuscaMinas = findViewById<Button>(R.id.btnPlayBuscaMinas)
        imageBuscaMinas.setOnClickListener { btnPlayBuscaMinas.visibility = View.VISIBLE }
        btnPlayBuscaMinas.setOnClickListener {
            val intent = Intent(this, Buscaminas::class.java)
            startActivity(intent)
        }

        // --------- NUEVO: Wiring Tetris (imagen + botón) ---------
        // Cargar GIF de Tetris si existe en res/raw/tetris (si no existe, Glide simplemente no mostrará animación)
        val imageTetris = findViewById<ImageView>(R.id.imageTetris)
        val tetrisUri = Uri.parse("android.resource://" + packageName + "/raw/tetris")
        Glide.with(this).asGif().load(tetrisUri).into(imageTetris)
        val btnPlayTetris = findViewById<Button>(R.id.btnPlayTetris)

        // Mostrar el botón al tocar la imagen (mismo patrón que otros juegos)
        imageTetris.setOnClickListener { btnPlayTetris.visibility = View.VISIBLE }

        // Por ahora mostramos un Toast. En el siguiente paso, lo reemplazamos por Intent hacia TetrisActivity
        btnPlayTetris.setOnClickListener {
            // TODO: Reemplazar por: startActivity(Intent(this, TetrisActivity::class.java)) cuando esté creada y registrada
            Toast.makeText(this, "Tetris estará disponible en unos instantes", Toast.LENGTH_SHORT).show()
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
        // No ExoPlayer instances to release for Snake/Memorama/BuscaMinas (usamos GIFs)
    }
}
