package com.example.proyecto_final

import android.content.Context
import android.view.MotionEvent
import android.widget.Toast

class ControladorToque_p {

    fun manejarToque(event: MotionEvent, logica: LogicaJuego_p, context: Context): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                // Mover la paleta del jugador según la posición del toque
                logica.moverPaletaJugador(event.x)
                return true
            }
        }
        return false
    }
}
