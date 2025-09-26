package com.example.proyecto_final.Juegos

import android.content.Context
import android.view.MotionEvent

class LogicaJuego_p(private val context: Context) {
    // Dimensiones de la pantalla
    var anchoPantalla = 0f
    var altoPantalla = 0f

    // Pelota
    var pelotaX = 0f
    var pelotaY = 0f
    var pelotaVelX = 5f
    var pelotaVelY = 5f
    val pelotaRadio = 20f

    // Paletas
    val paletaAncho = 200f
    val paletaAlto = 30f
    var paletaJugadorX = 0f
    var paletaJugadorY = 0f
    var paletaIAX = 0f
    var paletaIAY = 0f

    // Puntuación
    var puntajeJugador = 0
    var puntajeIA = 0
    var mejorPuntuacion = 0

    // Estado del juego
    var juegoActivo = true
    var gameOver = false
    var bounceCount = 0

    // Propiedades de compatibilidad para otras clases - CORREGIDAS
    val canvasWidth: Float get() = anchoPantalla
    val canvasHeight: Float get() = altoPantalla
    val ballX: Float get() = pelotaX  // Cambiado a val (solo lectura)
    val ballY: Float get() = pelotaY  // Cambiado a val (solo lectura)
    val ballRadius: Float get() = pelotaRadio
    val paddleWidth: Float get() = paletaAncho
    val paddleHeight: Float get() = paletaAlto
    var paddleX: Float
        get() = paletaJugadorX
        set(value) { paletaJugadorX = value }
    val enemyPaddleWidth: Float get() = paletaAncho
    val enemyPaddleHeight: Float get() = paletaAlto
    var enemyPaddleX: Float
        get() = paletaIAX
        set(value) { paletaIAX = value }
    val score: Int get() = puntajeJugador  // Cambiado a val (solo lectura)
    val highScore: Int get() = mejorPuntuacion  // Cambiado a val (solo lectura)

    fun inicializar(ancho: Int, alto: Int) {
        anchoPantalla = ancho.toFloat()
        altoPantalla = alto.toFloat()

        // Posición inicial de la pelota (centro)
        pelotaX = anchoPantalla / 2
        pelotaY = altoPantalla / 2

        // Posición inicial de las paletas
        paletaJugadorX = (anchoPantalla - paletaAncho) / 2
        paletaJugadorY = altoPantalla - paletaAlto - 50f

        paletaIAX = (anchoPantalla - paletaAncho) / 2
        paletaIAY = 50f

        // Reiniciar estado del juego
        juegoActivo = true
        gameOver = false
        puntajeJugador = 0
        puntajeIA = 0
        bounceCount = 0
    }

    fun configurarDimensiones(ancho: Int, alto: Int) {
        anchoPantalla = ancho.toFloat()
        altoPantalla = alto.toFloat()
    }

    fun actualizar() {
        if (!juegoActivo || gameOver) return

        // Mover la pelota
        pelotaX += pelotaVelX
        pelotaY += pelotaVelY

        // Rebote en las paredes laterales
        if (pelotaX - pelotaRadio <= 0 || pelotaX + pelotaRadio >= anchoPantalla) {
            pelotaVelX = -pelotaVelX
        }

        // Verificar colisión con paleta del jugador
        if (pelotaY + pelotaRadio >= paletaJugadorY &&
            pelotaX >= paletaJugadorX &&
            pelotaX <= paletaJugadorX + paletaAncho) {
            pelotaVelY = -Math.abs(pelotaVelY)
            bounceCount++
        }

        // Verificar colisión con paleta de la IA
        if (pelotaY - pelotaRadio <= paletaIAY + paletaAlto &&
            pelotaX >= paletaIAX &&
            pelotaX <= paletaIAX + paletaAncho) {
            pelotaVelY = Math.abs(pelotaVelY)
            bounceCount++
        }

        // Verificar si alguien anotó
        if (pelotaY - pelotaRadio <= 0) {
            // Punto para el jugador
            puntajeJugador++
            if (puntajeJugador > mejorPuntuacion) mejorPuntuacion = puntajeJugador
            reiniciarPelota()
        } else if (pelotaY + pelotaRadio >= altoPantalla) {
            // Punto para la IA - Game Over
            puntajeIA++
            gameOver = true
            juegoActivo = false
        }

        // Mover la IA (sigue la pelota)
        val centroIA = paletaIAX + paletaAncho / 2
        if (pelotaX < centroIA) {
            paletaIAX -= 4f
        } else if (pelotaX > centroIA) {
            paletaIAX += 4f
        }

        // Mantener la IA dentro de los límites
        if (paletaIAX < 0) paletaIAX = 0f
        if (paletaIAX + paletaAncho > anchoPantalla) paletaIAX = anchoPantalla - paletaAncho
    }

    private fun reiniciarPelota() {
        pelotaX = anchoPantalla / 2
        pelotaY = altoPantalla / 2
        pelotaVelX = if (Math.random() > 0.5) 5f else -5f
        pelotaVelY = if (Math.random() > 0.5) 5f else -5f
    }

    fun moverPaletaJugador(x: Float) {
        paletaJugadorX = x - paletaAncho / 2
        // Mantener dentro de los límites
        if (paletaJugadorX < 0) paletaJugadorX = 0f
        if (paletaJugadorX + paletaAncho > anchoPantalla) paletaJugadorX = anchoPantalla - paletaAncho
    }

    fun manejarToque(event: MotionEvent): Boolean {
        if (gameOver) {
            // Manejar toques en pantalla de game over
            return manejarToqueGameOver(event)
        }

        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                moverPaletaJugador(event.x)
                return true
            }
        }
        return false
    }

    private fun manejarToqueGameOver(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y

            // Botón "Jugar de nuevo" (simplificado)
            if (y > altoPantalla * 0.6f && y < altoPantalla * 0.7f) {
                resetGame()
                return true
            }
        }
        return false
    }

    fun resetGame() {
        juegoActivo = true
        gameOver = false
        puntajeJugador = 0
        puntajeIA = 0
        bounceCount = 0
        reiniciarPelota()
    }

    fun ajustarDificultad() {
        // Aumentar dificultad progresivamente
        if (bounceCount % 5 == 0 && bounceCount > 0) {
            pelotaVelX *= 1.05f
            pelotaVelY *= 1.05f
        }
    }
}