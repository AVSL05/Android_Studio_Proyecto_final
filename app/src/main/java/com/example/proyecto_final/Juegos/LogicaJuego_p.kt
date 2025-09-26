package com.example.pong

import android.content.Context
import android.content.SharedPreferences
import android.view.MotionEvent

class LogicaJuego_p(context: Context) {

    // Elementos del juego
    var canvasWidth = 0
    var canvasHeight = 0
    var paddleWidth = 0
    var paddleHeight = 0
    var paddleX = 0f
    var enemyPaddleWidth = 0
    var enemyPaddleHeight = 0
    var enemyPaddleX = 0f
    var ballX = 0f
    var ballY = 0f
    val ballRadius = 20f

    // Estado del juego
    var score = 0
    var highScore = 0
    var gameOver = false
    var bounceCount = 0

    // Gestores especializados
    private lateinit var prefs: SharedPreferences
    private lateinit var gestorVelocidad: VelocidadPelota_p
    private lateinit var gestorColisiones: GestorColisiones_p
    private lateinit var gestorEnemigo: GestorEnemigo_p
    private lateinit var gestorUI: GestorUI_p

    init {
        prefs = context.getSharedPreferences("PongPrefs", Context.MODE_PRIVATE)
        gestorVelocidad = VelocidadPelota_p()
        gestorColisiones = GestorColisiones_p(this, gestorVelocidad)
        gestorEnemigo = GestorEnemigo_p(this)
        gestorUI = GestorUI_p(this, prefs)
    }

    fun inicializar(width: Int, height: Int) {
        canvasWidth = width
        canvasHeight = height
        configurarDimensiones(width, height)
        resetGame()
    }

    fun configurarDimensiones(width: Int, height: Int) {
        canvasWidth = width
        canvasHeight = height

        paddleWidth = width / 4
        paddleHeight = height / 40
        enemyPaddleWidth = width / 4
        enemyPaddleHeight = height / 40

        paddleX = (width - paddleWidth) / 2f
        enemyPaddleX = (width - enemyPaddleWidth) / 2f

        gestorVelocidad.inicializar(width, height)
        resetBall()
    }

    fun actualizar() {
        if (gameOver) return

        // Mover la pelota con velocidad más notable
        ballX += gestorVelocidad.getVelocidadX()
        ballY += gestorVelocidad.getVelocidadY()

        // Rebotes en bordes laterales
        gestorColisiones.manejarRebotesParedes()

        // Inteligencia del enemigo
        gestorEnemigo.actualizar()

        // Colisiones
        gestorColisiones.manejarColisiones()

        // Verificar game over
        if (ballY + ballRadius > canvasHeight) {
            gameOver = true
            gestorUI.actualizarHighScore()
        }
    }

    fun manejarToque(event: MotionEvent): Boolean {
        if (gameOver) {
            return gestorUI.manejarToqueGameOver(event)
        } else {
            when (event.action) {
                MotionEvent.ACTION_MOVE, MotionEvent.ACTION_DOWN -> {
                    paddleX = event.x - paddleWidth / 2f
                    if (paddleX < 0) paddleX = 0f
                    if (paddleX > canvasWidth - paddleWidth) paddleX = (canvasWidth - paddleWidth).toFloat()
                    return true
                }
            }
            return false
        }
    }

    fun resetGame() {
        score = 0
        bounceCount = 0
        gameOver = false
        highScore = prefs.getInt("highScore", 0)
        gestorColisiones.reset()
        resetBall()
    }

    private fun resetBall() {
        ballX = canvasWidth / 2f
        ballY = canvasHeight / 2f
        gestorVelocidad.resetVelocidad()
        gestorColisiones.reset()
    }

    fun ajustarDificultad() {
        // PROGRESIÓN DE VELOCIDAD MÁS NOTORIA
        when {
            bounceCount <= 5 -> {
                gestorVelocidad.aumentarDificultadProgresiva()
                println("AUMENTO DIFICULTAD - Rebote: $bounceCount")
            }
            bounceCount == 6 -> {
                gestorVelocidad.disminuirVelocidad()
                println("DESCANSO VELOCIDAD - Rebote: $bounceCount")
            }
            else -> {
                gestorVelocidad.continuarProgresion()
                println("CONTINUACIÓN PROGRESIÓN - Rebote: $bounceCount")
            }
        }
    }

    fun getGestorVelocidad(): VelocidadPelota_p = gestorVelocidad
}