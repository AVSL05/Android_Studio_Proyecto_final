package com.example.proyecto_final.Juegos

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.proyecto_final.MainActivity
import com.example.proyecto_final.R
import kotlin.math.sqrt
import java.util.Random

class Snake : Activity() {

    // --- Estado UI / menú
    private var inMenu: Boolean = true
    private var selectedIndex: Int = 0 // 0 = Nuevo Juego, 1 = Menú Principal

    // --- Estado juego
    private val handler = Handler(Looper.getMainLooper())
    private var delayMillis = 30L
    private var currentDirection = "right"
    private var scoreValue = 0
    private var halfW = 0f
    private var halfH = 0f
    private val speed = 5f

    // Colores para el botón B
    private val snakeColors = listOf(Color.GREEN, Color.CYAN, Color.YELLOW, Color.MAGENTA, Color.WHITE)
    private var colorIdx = 0

    // Views
    private lateinit var board: RelativeLayout
    private lateinit var overlay: RelativeLayout
    private lateinit var btnNewGame: Button
    private lateinit var btnMenu: Button
    private lateinit var btnResume: Button
    private lateinit var btnPlayAgain: Button
    private var scoreField: EditText? = null
    private var scoreBtn: Button? = null
    private lateinit var btnExitSnake: Button

    private lateinit var upBtn: View
    private lateinit var downBtn: View
    private lateinit var leftBtn: View
    private lateinit var rightBtn: View

    private lateinit var btnSelect: View
    private lateinit var btnStart: View
    private lateinit var btnA: View
    private lateinit var btnB: View

    // Sprites
    private lateinit var meat: ImageView
    private lateinit var snakeHead: ImageView
    private val snakeSegments = mutableListOf<ImageView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Usa el layout correcto (cámbialo si tu archivo XML tiene otro nombre)
        setContentView(R.layout.activity_snake)

        // --- Referencias del layout ---
        board = findViewById(R.id.board)
        overlay = findViewById(R.id.overlay)
        btnNewGame = findViewById(R.id.new_game)
        btnMenu = findViewById(R.id.menu_btn)
        btnResume = findViewById(R.id.resume)
        btnPlayAgain = findViewById(R.id.playagain)
        scoreField = findViewById(R.id.scoreField)     // EditText con hint "Your Score"
        scoreBtn = findViewById(R.id.score2)           // fallback si aún usas Button
        btnExitSnake = findViewById(R.id.btnExitSnake)

        upBtn = findViewById(R.id.up)
        downBtn = findViewById(R.id.down)
        leftBtn = findViewById(R.id.left)
        rightBtn = findViewById(R.id.right)

        btnSelect = findViewById(R.id.btnSelect)
        btnStart  = findViewById(R.id.btnStart)
        btnA      = findViewById(R.id.btnA)
        btnB      = findViewById(R.id.btnB)

        // Sprites
        meat = ImageView(this)
        snakeHead = ImageView(this)

        // Salir al menú principal
        btnExitSnake.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            })
            finish()
        }

        // Estado inicial
        showMenu(initial = true)

        // ============ Wiring de controles ============
        // D-Pad
        upBtn.setOnClickListener    { if (currentDirection != "down")  currentDirection = "up" }
        downBtn.setOnClickListener  { if (currentDirection != "up")    currentDirection = "down" }
        leftBtn.setOnClickListener  { if (currentDirection != "right") currentDirection = "left" }
        rightBtn.setOnClickListener { if (currentDirection != "left")  currentDirection = "right" }

        // A -> Pausar/Reanudar
        btnA.setOnClickListener {
            if (inMenu) return@setOnClickListener
            if (isGameRunning()) {
                pauseGame()
                // mostrar opción Reanudar en overlay
                btnResume.visibility = View.VISIBLE
                overlay.visibility = View.VISIBLE
                inMenu = true
                // Ninguna de las dos opciones principales resaltada
                highlightSelection(-1)
                btnResume.alpha = 1f
            } else {
                resumeGame()
                hideMenu()
            }
        }

        // B -> Cambiar color serpiente
        btnB.setOnClickListener {
            colorIdx = (colorIdx + 1) % snakeColors.size
            setSnakeColor(snakeColors[colorIdx])
        }

        // SELECT -> Alternar opción en menú
        btnSelect.setOnClickListener {
            if (!inMenu) return@setOnClickListener
            // si está visible Resume, dejamos que Start lo ejecute aparte
            toggleSelection()
        }

        // START -> Ejecuta opción seleccionada (o inicia si no estás en menú)
        btnStart.setOnClickListener {
            if (inMenu) {
                if (btnResume.visibility == View.VISIBLE && selectedIndex !in 0..1) {
                    resumeGame()
                    hideMenu()
                    return@setOnClickListener
                }
                when (selectedIndex) {
                    0 -> { startNewGame(); hideMenu() }
                    1 -> { goToMainMenu() } // aquí navega como prefieras
                }
            } else {
                if (!isGameRunning()) startNewGame()
            }
        }

        // También permite tocar los botones visibles del overlay
        btnNewGame.setOnClickListener { startNewGame(); hideMenu() }
        btnMenu.setOnClickListener    { goToMainMenu() }
        btnResume.setOnClickListener  { resumeGame(); hideMenu() }
        btnPlayAgain.setOnClickListener { recreate() }
    }

    // ================== Menú ==================
    private fun showMenu(initial: Boolean = false) {
        overlay.visibility = View.VISIBLE
        board.visibility = View.INVISIBLE

        btnNewGame.visibility = View.VISIBLE
        btnMenu.visibility = View.VISIBLE
        btnResume.visibility = View.GONE
        btnPlayAgain.visibility = View.GONE

        inMenu = true
        selectedIndex = 0
        highlightSelection(selectedIndex)

        // score UI
        scoreField?.apply { setText(""); hint = "Your Score" }
        scoreBtn?.visibility = View.INVISIBLE
    }

    private fun hideMenu() {
        overlay.visibility = View.GONE
        board.visibility = View.VISIBLE
        inMenu = false
        clearSelectionVisuals()
    }

    private fun toggleSelection() {
        selectedIndex = if (selectedIndex == 0) 1 else 0
        highlightSelection(selectedIndex)
    }

    private fun highlightSelection(index: Int) {
        when (index) {
            0 -> { btnNewGame.alpha = 1f; btnMenu.alpha = .6f }
            1 -> { btnNewGame.alpha = .6f; btnMenu.alpha = 1f }
            else -> { btnNewGame.alpha = .6f; btnMenu.alpha = .6f }
        }
    }

    private fun clearSelectionVisuals() {
        btnNewGame.alpha = 1f
        btnMenu.alpha = 1f
    }

    // ================== Juego ==================
    private fun startNewGame() {
        // Reset UI
        btnResume.visibility = View.GONE
        btnPlayAgain.visibility = View.GONE
        scoreBtn?.visibility = View.VISIBLE
        scoreBtn?.text = "score : 0"
        scoreField?.setText("") // deja solo el hint

        board.removeAllViews()
        snakeSegments.clear()
        scoreValue = 0
        currentDirection = "right"
        delayMillis = 30L

        // Cabeza
        snakeHead.setImageResource(R.drawable.ic_snake)
        snakeHead.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        board.addView(snakeHead)
        snakeSegments.add(snakeHead)

        // Comida
        meat.setImageResource(R.drawable.meat)
        meat.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        board.addView(meat)

        // color actual a todos
        setSnakeColor(snakeColors[colorIdx])

        board.post {
            halfW = (board.width  - snakeHead.width) / 2f
            halfH = (board.height - snakeHead.height) / 2f

            var snakeX = 0f
            var snakeY = 0f
            snakeHead.translationX = snakeX
            snakeHead.translationY = snakeY

            fun dropFood() {
                val rnd = Random()
                val mx = (halfW - meat.width / 2f - 8f).coerceAtLeast(8f)
                val my = (halfH - meat.height / 2f - 8f).coerceAtLeast(8f)
                val rx = -mx + rnd.nextFloat() * (2f * mx)
                val ry = -my + rnd.nextFloat() * (2f * my)
                meat.translationX = rx
                meat.translationY = ry
            }
            dropFood()

            fun checkFoodCollision() {
                val dx = snakeHead.translationX - meat.translationX
                val dy = snakeHead.translationY - meat.translationY
                val distance = sqrt(dx*dx + dy*dy)
                val threshold = (snakeHead.width.coerceAtLeast(snakeHead.height)) * 0.8f

                if (distance < threshold) {
                    val seg = ImageView(this)
                    seg.setImageResource(R.drawable.ic_snake)
                    seg.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    seg.translationX = snakeSegments.last().translationX
                    seg.translationY = snakeSegments.last().translationY
                    board.addView(seg)
                    snakeSegments.add(seg)
                    // aplica color al nuevo segmento
                    setSnakeColor(snakeColors[colorIdx])

                    dropFood()

                    scoreValue++
                    scoreBtn?.text = "score : $scoreValue"
                    scoreField?.setText(scoreValue.toString())
                    if (delayMillis > 6) delayMillis -= 1
                }
            }

            fun gameOver() {
                currentDirection = "pause"
                // Muestra puntaje en el overlay (si tienes un TextView/Button score)
                // Aquí reutilizamos playAgain visible:
                btnPlayAgain.visibility = View.VISIBLE
                // Oculta botón score si lo usas
                scoreBtn?.visibility = View.INVISIBLE
            }

            val runnable = object : Runnable {
                override fun run() {
                    // Mover cuerpo
                    for (i in snakeSegments.size - 1 downTo 1) {
                        snakeSegments[i].translationX = snakeSegments[i - 1].translationX
                        snakeSegments[i].translationY = snakeSegments[i - 1].translationY
                    }

                    when (currentDirection) {
                        "up" -> {
                            snakeY -= speed
                            if (snakeY < -halfH) { snakeY = -halfH; gameOver() }
                            snakeHead.translationY = snakeY
                        }
                        "down" -> {
                            snakeY += speed
                            if (snakeY > halfH) { snakeY = halfH; gameOver() }
                            snakeHead.translationY = snakeY
                        }
                        "left" -> {
                            snakeX -= speed
                            if (snakeX < -halfW) { snakeX = -halfW; gameOver() }
                            snakeHead.translationX = snakeX
                        }
                        "right" -> {
                            snakeX += speed
                            if (snakeX > halfW) { snakeX = halfW; gameOver() }
                            snakeHead.translationX = snakeX
                        }
                        "pause" -> { /* no-op */ }
                    }

                    if (currentDirection != "pause") {
                        checkFoodCollision()
                        handler.postDelayed(this, delayMillis)
                    }
                }
            }

            handler.postDelayed(runnable, delayMillis)

            // Resume desde overlay
            btnResume.setOnClickListener {
                resumeGame()
                handler.postDelayed(runnable, delayMillis)
            }

            // Play again (reinicia actividad)
            btnPlayAgain.setOnClickListener { recreate() }
        }
    }

    private fun pauseGame() {
        currentDirection = "pause"
    }

    private fun resumeGame() {
        currentDirection = "right"
    }

    private fun isGameRunning(): Boolean = currentDirection != "pause"

    private fun setSnakeColor(color: Int) {
        // Aplica tinte a cabeza y segmentos existentes
        snakeHead.setColorFilter(color)
        for (i in 1 until snakeSegments.size) {
            snakeSegments[i].setColorFilter(color)
        }
        // Si quieres repintar carne/food, no lo toques.
    }

    private fun goToMainMenu() {
        // Aquí decides: cambiar de Activity o mostrar más opciones
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
