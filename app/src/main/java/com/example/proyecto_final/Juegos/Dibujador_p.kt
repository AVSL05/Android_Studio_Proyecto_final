package com.example.proyecto_final

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class Dibujador_p {
    private val paint = Paint()

    fun dibujar(canvas: Canvas, logica: LogicaJuego_p) {
        // Limpiar pantalla
        canvas.drawColor(Color.BLACK)

        // Configurar paint para elementos blancos
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL

        // Dibujar pelota
        canvas.drawCircle(logica.pelotaX, logica.pelotaY, logica.pelotaRadio, paint)

        // Dibujar paleta del jugador
        canvas.drawRect(
            logica.paletaJugadorX,
            logica.paletaJugadorY,
            logica.paletaJugadorX + logica.paletaAncho,
            logica.paletaJugadorY + logica.paletaAlto,
            paint
        )

        // Dibujar paleta de la IA
        canvas.drawRect(
            logica.paletaIAX,
            logica.paletaIAY,
            logica.paletaIAX + logica.paletaAncho,
            logica.paletaIAY + logica.paletaAlto,
            paint
        )

        // Dibujar línea central
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        canvas.drawLine(0f, logica.altoPantalla / 2, logica.anchoPantalla, logica.altoPantalla / 2, paint)

        // Dibujar puntuación
        paint.style = Paint.Style.FILL
        paint.textSize = 60f
        paint.textAlign = Paint.Align.CENTER

        // Puntuación IA (arriba)
        canvas.drawText(
            logica.puntajeIA.toString(),
            logica.anchoPantalla / 2,
            logica.altoPantalla / 2 - 50f,
            paint
        )

        // Puntuación Jugador (abajo)
        canvas.drawText(
            logica.puntajeJugador.toString(),
            logica.anchoPantalla / 2,
            logica.altoPantalla / 2 + 100f,
            paint
        )
    }
}
