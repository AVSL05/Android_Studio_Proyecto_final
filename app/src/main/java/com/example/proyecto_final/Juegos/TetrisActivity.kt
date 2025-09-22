package com.example.proyecto_final.Juegos

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_final.MainActivity
import com.example.proyecto_final.R

/**
 * Activity de Tetris
 * - Conecta la lógica pura (TetrisGame) con la UI Android
 * - Dibuja tablero y vista previa de la siguiente pieza
 * - Maneja controles (mover, rotar, caída) y bucle de juego por temporizador
 */
class TetrisActivity : AppCompatActivity() {

    // Lógica del juego
    private lateinit var game: TetrisGame

    // Vistas
    private lateinit var boardContainer: FrameLayout
    private lateinit var nextContainer: FrameLayout
    private lateinit var tvScore: TextView
    private lateinit var tvLevel: TextView
    private lateinit var tvLines: TextView

    // Vistas de dibujo personalizadas
    private lateinit var boardView: BoardView
    private lateinit var nextView: NextView

    // Temporizador del juego (tick)
    private val handler = Handler(Looper.getMainLooper())
    private var isPaused = false

    // Runnable que avanza el juego periódicamente
    private val tickRunnable = object : Runnable {
        override fun run() {
            if (!isPaused && !game.isGameOver && game.isRunning) {
                game.step()
                scheduleNextTick()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tetris)

        // Referencias a vistas del layout
        boardContainer = findViewById(R.id.boardContainer)
        nextContainer = findViewById(R.id.nextContainer)
        tvScore = findViewById(R.id.tvScore)
        tvLevel = findViewById(R.id.tvLevel)
        tvLines = findViewById(R.id.tvLines)

        // Crear y agregar vistas de dibujo dentro de los contenedores
        boardView = BoardView(this)
        boardContainer.addView(boardView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))

        nextView = NextView(this)
        nextContainer.addView(nextView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))

        // Instanciar juego y enlazar listener para refrescar UI
        game = TetrisGame().apply {
            listener = object : TetrisGame.Listener {
                override fun onBoardUpdated() {
                    // Redibujar tablero y preview y actualizar textos
                    boardView.invalidate()
                    nextView.invalidate()
                    updateHud()
                }
                override fun onPieceLocked() { /* opcional: sonido/efecto */ }
                override fun onLinesCleared(lines: Int) {
                    // Mostrar retroalimentación simple
                    Toast.makeText(this@TetrisActivity, "Líneas: +$lines", Toast.LENGTH_SHORT).show()
                }
                override fun onGameOver() {
                    updateHud()
                    Toast.makeText(this@TetrisActivity, "Game Over", Toast.LENGTH_LONG).show()
                    // detener ticks
                    handler.removeCallbacks(tickRunnable)
                }
            }
        }

        // Iniciar partida
        game.start()
        updateHud()
        scheduleNextTick()

        // Controles
        val btnLeft = findViewById<Button>(R.id.btnLeft)
        val btnRight = findViewById<Button>(R.id.btnRight)
        val btnRotate = findViewById<Button>(R.id.btnRotate)
        val btnSoftDrop = findViewById<Button>(R.id.btnSoftDrop)
        val btnHardDrop = findViewById<Button>(R.id.btnHardDrop)
        val btnPause = findViewById<Button>(R.id.btnPause)
        val btnExit = findViewById<Button>(R.id.btnExit)

        // Mover a la izquierda
        btnLeft.setOnClickListener { game.moveLeft() }
        // Mover a la derecha
        btnRight.setOnClickListener { game.moveRight() }
        // Rotar 90° sentido horario (⟳)
        btnRotate.setOnClickListener { game.rotate() }
        // Caída suave (baja 1 bloque)
        btnSoftDrop.setOnClickListener { game.softDrop() }
        // Caída dura (hasta el fondo y fija)
        btnHardDrop.setOnClickListener { game.hardDrop() }
        // Pausar/Reanudar juego
        btnPause.setOnClickListener {
            isPaused = !isPaused
            btnPause.text = if (isPaused) "Reanudar" else "Pausa"
            if (!isPaused && !game.isGameOver) scheduleNextTick() else handler.removeCallbacks(tickRunnable)
        }
        // Salir a menú principal
        btnExit.setOnClickListener {
            handler.removeCallbacks(tickRunnable)
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        // Pausar ticks al ir a segundo plano
        handler.removeCallbacks(tickRunnable)
    }

    override fun onResume() {
        super.onResume()
        // Reanudar si no está pausado manualmente ni en game over
        if (!isPaused && !::game.isInitialized.not() && game.isRunning && !game.isGameOver) scheduleNextTick()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(tickRunnable)
    }

    // Actualizar textos de HUD (puntuación/nivel/líneas)
    private fun updateHud() {
        tvScore.text = getString(R.string.tetris_score, game.score)
        tvLevel.text = getString(R.string.tetris_level, game.level)
        tvLines.text = getString(R.string.tetris_lines, game.linesCleared)
    }

    // Programar el siguiente tick según el nivel
    private fun scheduleNextTick() {
        handler.removeCallbacks(tickRunnable)
        val base = 700L // ms base nivel 1
        val step = 60L  // decremento por nivel
        val min = 90L   // mínimo
        val delay = (base - (game.level - 1) * step).coerceAtLeast(min)
        handler.postDelayed(tickRunnable, delay)
    }

    /**
     * Vista de tablero: dibuja la matriz renderizada del juego
     */
    private inner class BoardView(context: android.content.Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            // Fondo
            canvas.drawColor(Color.BLACK)

            val cols = game.width
            val rows = game.height
            val cellW = width / cols.toFloat()
            val cellH = height / rows.toFloat()

            // Dibujar celdas
            val board = game.getRenderBoard()
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val id = board[r][c]
                    if (id != 0) {
                        paint.color = colorForId(id)
                        val left = c * cellW
                        val top = r * cellH
                        canvas.drawRect(left, top, left + cellW, top + cellH, paint)
                    }
                }
            }
            // Opcional: rejilla
            for (r in 0..rows) {
                val y = r * cellH
                canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
            }
            for (c in 0..cols) {
                val x = c * cellW
                canvas.drawLine(x, 0f, x, height.toFloat(), gridPaint)
            }
        }
    }

    /**
     * Vista de la siguiente pieza: dibuja el shape en un área cuadrada
     */
    private inner class NextView(context: android.content.Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val bgPaint = Paint().apply { color = Color.rgb(34, 34, 34) }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            // Fondo del preview
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            val shape = game.getNextShape()
            // Dimensiones de la forma
            val rows = shape.size
            val cols = shape[0].size
            val size = kotlin.math.min(width, height).toFloat()
            val cell = size / kotlin.math.max(rows, cols)

            // Centrar
            val offsetX = (width - cols * cell) / 2f
            val offsetY = (height - rows * cell) / 2f

            // Color real de la siguiente pieza
            val colorId = game.getNextColor()
            paint.color = colorForId(colorId)

            for (i in 0 until rows) {
                for (j in 0 until cols) {
                    if (shape[i][j] == 1) {
                        val left = offsetX + j * cell
                        val top = offsetY + i * cell
                        canvas.drawRect(left, top, left + cell, top + cell, paint)
                    }
                }
            }
        }
    }

    // Paleta de colores por id de pieza
    private fun colorForId(id: Int): Int = when (id) {
        1 -> Color.CYAN     // I
        2 -> Color.YELLOW   // O
        3 -> Color.MAGENTA  // T
        4 -> Color.GREEN    // S
        5 -> Color.RED      // Z
        6 -> Color.BLUE     // J
        7 -> Color.rgb(255, 165, 0) // L (naranja)
        else -> Color.LTGRAY
    }
}
