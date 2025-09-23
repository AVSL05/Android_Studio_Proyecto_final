package com.example.proyecto_final.Juegos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import com.example.proyecto_final.MainActivity
import com.example.proyecto_final.R
import kotlin.math.pow
import kotlin.math.sqrt
import java.util.Random

class Snake : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snake)

        // --- Referencias del layout (versión Game Boy) ---
        val board = findViewById<RelativeLayout>(R.id.board)          // “pantalla” negra
        val upBtn: View   = findViewById(R.id.up)                      // Views invisibles
        val downBtn: View = findViewById(R.id.down)
        val leftBtn: View = findViewById(R.id.left)
        val rightBtn: View = findViewById(R.id.right)
        val pauseBtn: View = findViewById(R.id.pause)

        val newgame = findViewById<Button>(R.id.new_game)
        val resume  = findViewById<Button>(R.id.resume)
        val playagain = findViewById<Button>(R.id.playagain)
        val scoreOverlay = findViewById<Button>(R.id.score)
        val score2 = findViewById<Button>(R.id.score2)
        val btnExitSnake = findViewById<Button>(R.id.btnExitSnake)

        // Sprites
        val meat = ImageView(this)
        val snakeHead = ImageView(this)
        val snakeSegments = mutableListOf<ImageView>() // [0] será la cabeza

        // Estado del juego
        val handler = Handler(Looper.getMainLooper())
        var delayMillis = 30L           // tick del juego
        var currentDirection = "right"  // inicio hacia la derecha
        var scoreValue = 0

        // Límites dinámicos de la “pantalla” (se calculan tras el layout)
        var halfW = 0f
        var halfH = 0f

        // --- Salir al menú principal ---
        btnExitSnake.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // Estado inicial UI
        board.visibility = View.INVISIBLE
        playagain.visibility = View.INVISIBLE
        scoreOverlay.visibility = View.INVISIBLE
        score2.visibility = View.INVISIBLE
        newgame.visibility = View.VISIBLE
        resume.visibility = View.INVISIBLE

        // ---------- NUEVO JUEGO ----------

        newgame.setOnClickListener {

            // Reset UI
            board.visibility = View.VISIBLE
            newgame.visibility = View.INVISIBLE
            resume.visibility = View.INVISIBLE
            score2.visibility = View.VISIBLE
            score2.text = "score : 0"
            scoreOverlay.visibility = View.GONE
            playagain.visibility = View.GONE

            // Limpia el board por si reiniciaste
            board.removeAllViews()
            snakeSegments.clear()
            scoreValue = 0
            currentDirection = "right"
            delayMillis = 30L

            // Crea cabeza
            snakeHead.setImageResource(R.drawable.snake)
            snakeHead.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            board.addView(snakeHead)
            snakeSegments.add(snakeHead)

            // Crea comida
            meat.setImageResource(R.drawable.meat)
            meat.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            board.addView(meat)

            // Cuando el board ya tenga tamaño, centra y calcula límites
            board.post {
                // Límites para usar translationX/Y con el sprite centrado
                halfW = (board.width  - snakeHead.width) / 2f
                halfH = (board.height - snakeHead.height) / 2f

                // Centra cabeza
                var snakeX = 0f
                var snakeY = 0f
                snakeHead.translationX = snakeX
                snakeHead.translationY = snakeY

                // Coloca comida en lugar aleatorio dentro de los límites
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
                        // Agrega un segmento nuevo detrás de la cabeza
                        val seg = ImageView(this)
                        seg.setImageResource(R.drawable.snake)
                        seg.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        seg.translationX = snakeSegments.last().translationX
                        seg.translationY = snakeSegments.last().translationY
                        board.addView(seg)
                        snakeSegments.add(seg)

                        dropFood()

                        // Sube score y acelera leve
                        scoreValue++
                        score2.text = "score : $scoreValue"
                        if (delayMillis > 6) delayMillis -= 1
                    }
                }

                fun gameOver() {
                    currentDirection = "pause"
                    scoreOverlay.text = "your score is  $scoreValue"
                    scoreOverlay.visibility = View.VISIBLE
                    score2.visibility = View.INVISIBLE
                    playagain.visibility = View.VISIBLE
                }

                val speed = 5f

                val runnable = object : Runnable {
                    override fun run() {

                        // Mover cuerpo: de la cola hacia la cabeza
                        for (i in snakeSegments.size - 1 downTo 1) {
                            snakeSegments[i].translationX = snakeSegments[i - 1].translationX
                            snakeSegments[i].translationY = snakeSegments[i - 1].translationY
                        }

                        when (currentDirection) {
                            "up" -> {
                                snakeY -= speed
                                if (snakeY < -halfH) {
                                    snakeY = -halfH
                                    gameOver()
                                }
                                snakeHead.translationY = snakeY
                            }
                            "down" -> {
                                snakeY += speed
                                if (snakeY > halfH) {
                                    snakeY = halfH
                                    gameOver()
                                }
                                snakeHead.translationY = snakeY
                            }
                            "left" -> {
                                snakeX -= speed
                                if (snakeX < -halfW) {
                                    snakeX = -halfW
                                    gameOver()
                                }
                                snakeHead.translationX = snakeX
                            }
                            "right" -> {
                                snakeX += speed
                                if (snakeX > halfW) {
                                    snakeX = halfW
                                    gameOver()
                                }
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

                // Controles
                upBtn.setOnClickListener    { if (currentDirection != "down")  currentDirection = "up" }
                downBtn.setOnClickListener  { if (currentDirection != "up")    currentDirection = "down" }
                leftBtn.setOnClickListener  { if (currentDirection != "right") currentDirection = "left" }
                rightBtn.setOnClickListener { if (currentDirection != "left")  currentDirection = "right" }

                pauseBtn.setOnClickListener {
                    if (currentDirection == "pause") {
                        currentDirection = "right"
                        board.visibility = View.VISIBLE
                        newgame.visibility = View.INVISIBLE
                        resume.visibility = View.INVISIBLE
                        handler.postDelayed(runnable, delayMillis)
                    } else {
                        currentDirection = "pause"
                        board.visibility = View.INVISIBLE
                        newgame.visibility = View.VISIBLE
                        resume.visibility = View.VISIBLE
                    }
                }

                resume.setOnClickListener {
                    currentDirection = "right"
                    board.visibility = View.VISIBLE
                    newgame.visibility = View.INVISIBLE
                    resume.visibility = View.INVISIBLE
                    handler.postDelayed(runnable, delayMillis)
                }

                playagain.setOnClickListener { recreate() }
            } // end board.post { ... }
        } // end newgame.setOnClickListener
    }
}
