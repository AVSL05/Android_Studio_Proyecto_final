package com.example.pong

import android.view.MotionEvent
import android.content.Context

class ControladorToque_p {

    fun manejarToque(event: MotionEvent, logica: LogicaJuego_p, context: Context): Boolean {
        return logica.manejarToque(event)
    }
}