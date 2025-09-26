package com.example.proyecto_final

import android.content.Context
import android.graphics.RectF

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
    var highScore = 0

    // Estado del juego
    var juegoActivo = true

    // Propiedades puente para compatibilidad con otros gestores
    val canvasWidth: Float get() = anchoPantalla
    val canvasHeight: Float get() = altoPantalla
    val enemyPaddleWidth: Float get() = paletaAncho
    var enemyPaddleX: Float
        get() = paletaIAX
        set(value) { paletaIAX = value }
    val ballX: Float get() = pelotaX
    val score: Int get() = puntajeJugador

    fun resetGame() = reiniciarJuego()

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
    }

    fun configurarDimensiones(ancho: Int, alto: Int) {
        anchoPantalla = ancho.toFloat()
        altoPantalla = alto.toFloat()
    }

    fun actualizar() {
        if (!juegoActivo) return

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
        }

        // Verificar colisión con paleta de la IA
        if (pelotaY - pelotaRadio <= paletaIAY + paletaAlto &&
            pelotaX >= paletaIAX &&
            pelotaX <= paletaIAX + paletaAncho) {
            pelotaVelY = Math.abs(pelotaVelY)
        }

        // Verificar si alguien anotó
        if (pelotaY - pelotaRadio <= 0) {
            // Punto para el jugador
            puntajeJugador++
            if (puntajeJugador > highScore) highScore = puntajeJugador
            reiniciarPelota()
        } else if (pelotaY + pelotaRadio >= altoPantalla) {
            // Punto para la IA
            puntajeIA++
            reiniciarPelota()
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

    fun reiniciarJuego() {
        puntajeJugador = 0
        puntajeIA = 0
        juegoActivo = true
        reiniciarPelota()
    }
}
