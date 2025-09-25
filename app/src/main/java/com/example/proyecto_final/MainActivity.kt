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
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.proyecto_final.Juegos.Buscaminas
import com.example.proyecto_final.Juegos.MemoramaActivity
import com.example.proyecto_final.Juegos.Snake
import com.example.proyecto_final.Juegos.SudokuActivity
import com.example.proyecto_final.utils.SessionManager
import android.widget.Toast
import com.example.proyecto_final.Juegos.TetrisActivity // Import de la Activity de Tetris
import com.example.proyecto_final.Juegos.BreakoutActivity // NUEVO: import de Breakout
import androidx.appcompat.widget.SwitchCompat

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
        val btnPlayMemorama = findViewById<Button>(R.id.btnPlayMemorama)
        val btnPlaySudoku = findViewById<Button>(R.id.btnPlaySudoku)
        val btnPlayBuscaMinas = findViewById<Button>(R.id.btnPlayBuscaMinas)
        val btnPlayTetris = findViewById<Button>(R.id.btnPlayTetris)
        val btnPlayBreakout = findViewById<Button>(R.id.btnPlayBreakout) // NUEVO: botón Breakout

        // Referencias a todos los botones de jugar (sin incluir todavía Breakout en listeners previos)
        val btnsJugar = listOf(btnPlaySnake, btnPlayMemorama, btnPlaySudoku, btnPlayBuscaMinas, btnPlayTetris, btnPlayBreakout)

        imageSnake.setOnClickListener {
            if (btnPlaySnake.visibility == View.VISIBLE) {
                btnPlaySnake.visibility = View.GONE
            } else {
                btnsJugar.forEach { it.visibility = View.GONE }
                btnPlaySnake.visibility = View.VISIBLE
            }
        }
        btnPlaySnake.setOnClickListener {
            val intent = Intent(this, Snake::class.java)
            startActivity(intent)
        }

        val imageMemorama = findViewById<ImageView>(R.id.imageMemorama)
        val memoramaUri = Uri.parse("android.resource://" + packageName + "/raw/memorama")
        Glide.with(this).asGif().load(memoramaUri).into(imageMemorama)

        imageMemorama.setOnClickListener {
            if (btnPlayMemorama.visibility == View.VISIBLE) {
                btnPlayMemorama.visibility = View.GONE
            } else {
                btnsJugar.forEach { it.visibility = View.GONE }
                btnPlayMemorama.visibility = View.VISIBLE
            }
        }
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
        imageSudoku.setOnClickListener {
            if (btnPlaySudoku.visibility == View.VISIBLE) {
                btnPlaySudoku.visibility = View.GONE
            } else {
                btnsJugar.forEach { it.visibility = View.GONE }
                btnPlaySudoku.visibility = View.VISIBLE
            }
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
        imageBuscaMinas.setOnClickListener {
            if (btnPlayBuscaMinas.visibility == View.VISIBLE) {
                btnPlayBuscaMinas.visibility = View.GONE
            } else {
                btnsJugar.forEach { it.visibility = View.GONE }
                btnPlayBuscaMinas.visibility = View.VISIBLE
            }
        }
        btnPlayBuscaMinas.setOnClickListener {
            val intent = Intent(this, Buscaminas::class.java)
            startActivity(intent)
        }

        // --------- Wiring Tetris (imagen + botón) ---------
        val imageTetris = findViewById<ImageView>(R.id.imageTetris)
        val tetrisUri = Uri.parse("android.resource://" + packageName + "/raw/tetris")
        Glide.with(this).asGif().load(tetrisUri).into(imageTetris)

        imageTetris.setOnClickListener {
            if (btnPlayTetris.visibility == View.VISIBLE) {
                btnPlayTetris.visibility = View.GONE
            } else {
                btnsJugar.forEach { it.visibility = View.GONE }
                btnPlayTetris.visibility = View.VISIBLE
            }
        }
        btnPlayTetris.setOnClickListener {
            // Abrir la nueva actividad de Tetris
            val intent = Intent(this, TetrisActivity::class.java)
            startActivity(intent)
        }

        // --------- NUEVO: Breakout (imagen + botón) ---------
        val imageBreakout = findViewById<ImageView>(R.id.imageBreakout)
        // Intentamos cargar un GIF/animación si existe en raw (nombre esperado: breakout.*)
        val breakoutResId = resources.getIdentifier("breakout", "raw", packageName)
        if (breakoutResId != 0) {
            // Cargar como GIF (si es .gif) o como recurso estático
            try { Glide.with(this).asGif().load(breakoutResId).into(imageBreakout) } catch (e: Exception) {
                Glide.with(this).load(breakoutResId).into(imageBreakout)
            }
        } // Si no existe, queda el background gris por defecto

        imageBreakout.setOnClickListener {
            if (btnPlayBreakout.visibility == View.VISIBLE) {
                btnPlayBreakout.visibility = View.GONE
            } else {
                btnsJugar.forEach { it.visibility = View.GONE }
                btnPlayBreakout.visibility = View.VISIBLE
            }
        }
        btnPlayBreakout.setOnClickListener {
            // Lanzar Activity de Breakout
            startActivity(Intent(this, BreakoutActivity::class.java))
        }

        // Preferencias para modo oscuro
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // Configurar el switch de modo oscuro
        val switchDarkModeMain = findViewById<SwitchCompat>(R.id.switchDarkModeMain)
        switchDarkModeMain.isChecked = isDarkMode
        switchDarkModeMain.setOnCheckedChangeListener { _, isChecked ->
            val editor = prefs.edit()
            editor.putBoolean("dark_mode", isChecked)
            editor.apply()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
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
        // No ExoPlayer instances to release for Snake/Memorama/BuscaMinas/Tetris/Breakout (usamos GIFs o imágenes)
    }
}
