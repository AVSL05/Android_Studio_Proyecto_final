package com.example.pong

import android.content.SharedPreferences
import android.view.MotionEvent

class GestorUI_p(
    private val logica: LogicaJuego_p,
    private val prefs: SharedPreferences
) {

    fun actualizarHighScore() {
        if (logica.score > logica.highScore) {
            logica.highScore = logica.score
            prefs.edit().putInt("highScore", logica.highScore).apply()
        }
    }

    fun manejarToqueGameOver(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y

            // Botón "Jugar de nuevo"
            if (x >= logica.canvasWidth * 0.2f && x <= logica.canvasWidth * 0.8f &&
                y >= logica.canvasHeight * 0.6f && y <= logica.canvasHeight * 0.7f) {
                logica.resetGame()
                return true
            }
        }
        return false
    }
}