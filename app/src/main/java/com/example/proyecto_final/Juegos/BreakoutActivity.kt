package com.example.proyecto_final.Juegos

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.max

/**
 * Activity del juego Breakout (pelota que rompe ladrillos)
 * - En esta primera versión la lógica y el render viven en un solo archivo para el primer commit
 * - No dependemos todavía de un layout XML: se crea una vista de juego (GameView) programáticamente
 * - Más adelante podremos extraer controles/score a un XML y actualizar el MainActivity y el Manifest
 */

class BreakoutActivity : AppCompatActivity() {

    // Referencia a la vista de juego

    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crear instancia de la vista y usarla como contenido

        gameView = GameView(this)
        setContentView(gameView)
    }

    override fun onResume() {
        super.onResume()
        gameView.resume()
    }

    override fun onPause() {
        super.onPause()
        gameView.pause()
    }

    private class GameView(context: Context) : SurfaceView(context), Runnable, SurfaceHolder.Callback {

        @Volatile private var running = false
        private var thread: Thread? = null

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 48f
        }

        // --- NUEVO: margen seguro superior (status bar) ---
        private var safeTop = 0f

        // Pelota
        private var ballX = 0f
        private var ballY = 0f
        private var ballR = 18f
        private var ballSpeedX = 8f
        private var ballSpeedY = -8f

        // Paleta
        private var paddleW = 260f
        private var paddleH = 28f
        private var paddleX = 0f
        private var paddleY = 0f
        private var paddleSpeed = 0f

        // Estado
        private var score = 0
        private var lives = 3
        private var isGameOver = false
        private var isWin = false

        // Ladrillos
        private data class Brick(var left: Float, var top: Float, var right: Float, var bottom: Float, var alive: Boolean = true)
        private val bricks = mutableListOf<Brick>()
        private var bricksPerRow = 8
        private var brickRows = 5
        private var brickHeight = 0f
        private var brickPadding = 8f

        private var lastTime = System.nanoTime()

        init {
            holder.addCallback(this)
            isFocusable = true

            // --- NUEVO: escuchar e inyectar insets para conocer el alto de la status bar ---
            ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
                val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
                safeTop = topInset.toFloat()
                insets
            }
        }

        override fun surfaceCreated(holder: SurfaceHolder) {
            resetLevel()
            resume()
        }
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
        override fun surfaceDestroyed(holder: SurfaceHolder) { pause() }

        override fun run() {
            while (running) {
                val now = System.nanoTime()
                val deltaMs = (now - lastTime) / 1_000_000f
                lastTime = now
                update(deltaMs)
                drawFrame()
                try { Thread.sleep(8) } catch (_: InterruptedException) {}
            }
        }

        fun resume() {
            if (running) return
            running = true
            lastTime = System.nanoTime()
            thread = Thread(this, "BreakoutLoop").also { it.start() }
        }

        fun pause() {
            running = false
            try { thread?.join() } catch (_: InterruptedException) {}
            thread = null
        }

        private fun resetLevel() {
            // Paleta
            paddleY = height - 140f
            paddleX = (width - paddleW) / 2f
            // Pelota
            ballX = paddleX + paddleW / 2f
            ballY = paddleY - ballR - 4f
            ballSpeedX = 7f * if ((0..1).random() == 0) 1 else -1
            ballSpeedY = -9f

            // Ladrillos
            bricks.clear()
            val totalPaddingX = (bricksPerRow + 1) * brickPadding
            val brickWidth = (width - totalPaddingX) / bricksPerRow
            brickHeight = 48f

            // --- CAMBIO: offset inicial de ladrillos usa safeTop para evitar que queden bajo la barra ---
            val bricksTopOffset = safeTop + 120f
            for (row in 0 until brickRows) {
                for (col in 0 until bricksPerRow) {
                    val left = brickPadding + col * (brickWidth + brickPadding)
                    val top = bricksTopOffset + row * (brickHeight + brickPadding)
                    val right = left + brickWidth
                    val bottom = top + brickHeight
                    bricks += Brick(left, top, right, bottom)
                }
            }
            isGameOver = false
            isWin = false
        }

        private fun update(deltaMs: Float) {
            if (isGameOver) return

            ballX += ballSpeedX
            ballY += ballSpeedY

            if (ballX - ballR < 0) { ballX = ballR; ballSpeedX = abs(ballSpeedX) }
            if (ballX + ballR > width) { ballX = width - ballR; ballSpeedX = -abs(ballSpeedX) }
            if (ballY - ballR < 0) { ballY = ballR; ballSpeedY = abs(ballSpeedY) }

            if (ballY - ballR > height) {
                lives--
                if (lives <= 0) {
                    isGameOver = true
                } else {
                    paddleX = (width - paddleW) / 2f
                    ballX = paddleX + paddleW / 2f
                    ballY = paddleY - ballR - 4f
                    ballSpeedY = -9f
                }
                return
            }

            if (ballY + ballR >= paddleY && ballY - ballR <= paddleY + paddleH &&
                ballX in paddleX..(paddleX + paddleW)) {
                ballY = paddleY - ballR - 1f
                val hitPos = (ballX - paddleX) / paddleW - 0.5f
                ballSpeedX = 12f * hitPos * 2f
                ballSpeedY = -abs(ballSpeedY)
            }

            var bricksAlive = 0
            for (b in bricks) {
                if (!b.alive) continue
                bricksAlive++
                if (ballX + ballR >= b.left && ballX - ballR <= b.right &&
                    ballY + ballR >= b.top && ballY - ballR <= b.bottom) {
                    b.alive = false
                    score += 10
                    val overlapLeft = ballX + ballR - b.left
                    val overlapRight = b.right - (ballX - ballR)
                    val overlapTop = ballY + ballR - b.top
                    val overlapBottom = b.bottom - (ballY - ballR)
                    val minOverlap = min(min(overlapLeft, overlapRight), min(overlapTop, overlapBottom))
                    when (minOverlap) {
                        overlapLeft -> ballSpeedX = -abs(ballSpeedX)
                        overlapRight -> ballSpeedX = abs(ballSpeedX)
                        overlapTop -> ballSpeedY = -abs(ballSpeedY)
                        overlapBottom -> ballSpeedY = abs(ballSpeedY)
                    }
                }
            }
            if (bricksAlive == 0) {
                isWin = true
                isGameOver = true
            }
        }

        private fun drawFrame() {
            val canvas = holder.lockCanvas() ?: return
            try {
                canvas.drawColor(Color.BLACK)

                for (b in bricks) if (b.alive) {
                    paint.color = Color.rgb(200, 200 - ((b.top / 6) % 120).toInt(), 80)
                    canvas.drawRect(b.left, b.top, b.right, b.bottom, paint)
                }

                paint.color = Color.WHITE
                canvas.drawRect(paddleX, paddleY, paddleX + paddleW, paddleY + paddleH, paint)

                paint.color = Color.CYAN
                canvas.drawCircle(ballX, ballY, ballR, paint)

                // --- CAMBIO: HUD usa safeTop para no quedar tapado por la status bar ---
                val hudY = safeTop + 60f
                canvas.drawText("Puntos: $score", 24f, hudY, textPaint)
                canvas.drawText("Vidas: $lives", width - 220f, hudY, textPaint)

                if (isGameOver) {
                    val msg = if (isWin) "¡GANASTE!" else "GAME OVER"
                    val sub = "Toca para reiniciar"
                    textPaint.textSize = 72f
                    val wMsg = textPaint.measureText(msg)
                    canvas.drawText(msg, (width - wMsg) / 2f, height / 2f, textPaint)
                    textPaint.textSize = 42f
                    val wSub = textPaint.measureText(sub)
                    canvas.drawText(sub, (width - wSub) / 2f, height / 2f + 70f, textPaint)
                    textPaint.textSize = 48f
                }
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    val target = event.x - paddleW / 2f
                    paddleX = max(0f, min(target, width - paddleW))
                    if (isGameOver) {
                        score = 0
                        lives = 3
                        resetLevel()
                    }
                }
            }
            return true
        }
    }
}
