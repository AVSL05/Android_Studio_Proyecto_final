package com.example.pong

import kotlin.math.abs

class GestorEnemigo_p(private val logica: LogicaJuego_p) {

    fun actualizar() {
        val enemyCenter = logica.enemyPaddleX + logica.enemyPaddleWidth / 2
        val ballCenter = logica.ballX
        val diff = ballCenter - enemyCenter

        // ENEMIGO 200% MÁS RÁPIDO (de 0.75f a 1.5f)
        if (abs(diff) > 2) {
            logica.enemyPaddleX += diff * 1.7f
        }

        // Mantener dentro de límites
        if (logica.enemyPaddleX < 0) logica.enemyPaddleX = 0f
        if (logica.enemyPaddleX > logica.canvasWidth - logica.enemyPaddleWidth) {
            logica.enemyPaddleX = (logica.canvasWidth - logica.enemyPaddleWidth).toFloat()
        }
    }
}