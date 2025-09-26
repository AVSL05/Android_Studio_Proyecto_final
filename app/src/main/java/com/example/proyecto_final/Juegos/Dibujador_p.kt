package com.example.proyecto_final.Juegos

import android.graphics.*

class Dibujador_p {
    private val paint = Paint()
    private lateinit var background: Bitmap

    fun dibujar(canvas: Canvas, logica: LogicaJuego_p) {
        // Dibujar fondo
        if (!::background.isInitialized || background.width != canvas.width || background.height != canvas.height) {
            crearFondo(canvas.width, canvas.height)
        }
        canvas.drawBitmap(background, 0f, 0f, paint)

        // Dibujar elementos del juego
        dibujarPaletas(canvas, logica)
        dibujarPelota(canvas, logica)
        dibujarLineaCentral(canvas, logica)
        dibujarUI(canvas, logica)

        if (logica.gameOver) {
            dibujarGameOver(canvas, logica)
        }
    }

    private fun crearFondo(width: Int, height: Int) {
        background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val bgCanvas = Canvas(background)
        bgCanvas.drawColor(Color.BLACK)
    }

    private fun dibujarPaletas(canvas: Canvas, logica: LogicaJuego_p) {
        // Paleta del jugador (abajo)
        paint.color = Color.BLUE
        canvas.drawRect(logica.paddleX, logica.paletaJugadorY,
            logica.paddleX + logica.paddleWidth, logica.paletaJugadorY + logica.paddleHeight, paint)

        // Paleta del enemigo (arriba)
        paint.color = Color.RED
        canvas.drawRect(logica.enemyPaddleX, logica.paletaIAY,
            logica.enemyPaddleX + logica.enemyPaddleWidth, logica.paletaIAY + logica.enemyPaddleHeight, paint)
    }

    private fun dibujarPelota(canvas: Canvas, logica: LogicaJuego_p) {
        paint.color = Color.YELLOW
        canvas.drawCircle(logica.ballX, logica.ballY, logica.ballRadius, paint)
    }

    private fun dibujarLineaCentral(canvas: Canvas, logica: LogicaJuego_p) {
        paint.color = Color.WHITE
        paint.strokeWidth = 3f
        canvas.drawLine(0f, logica.canvasHeight / 2f,
            logica.canvasWidth.toFloat(), logica.canvasHeight / 2f, paint)
    }

    private fun dibujarUI(canvas: Canvas, logica: LogicaJuego_p) {
        // Puntuación
        paint.textSize = 50f
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.WHITE
        canvas.drawText("Puntos: ${logica.score}", 30f, 60f, paint)

        // Mejor puntuación
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Mejor: ${logica.highScore}", (logica.canvasWidth - 30).toFloat(), 60f, paint)

        // Contador de rebotes
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 30f
        canvas.drawText("Rebotes: ${logica.bounceCount}", logica.canvasWidth / 2f, 100f, paint)
    }

    private fun dibujarGameOver(canvas: Canvas, logica: LogicaJuego_p) {
        // Fondo semitransparente
        paint.color = Color.argb(200, 0, 0, 0)
        canvas.drawRect(0f, 0f, logica.canvasWidth.toFloat(), logica.canvasHeight.toFloat(), paint)

        // Mensaje de game over
        paint.color = Color.WHITE
        paint.textSize = 60f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("¡Juego Terminado!", logica.canvasWidth / 2f, logica.canvasHeight / 3f, paint)

        paint.textSize = 40f
        canvas.drawText("Puntuación: ${logica.score}", logica.canvasWidth / 2f, logica.canvasHeight / 3f + 80, paint)
        canvas.drawText("Mejor: ${logica.highScore}", logica.canvasWidth / 2f, logica.canvasHeight / 3f + 130, paint)
        canvas.drawText("Rebotes: ${logica.bounceCount}", logica.canvasWidth / 2f, logica.canvasHeight / 3f + 180, paint)

        // Botón "Jugar de nuevo"
        paint.color = Color.GREEN
        canvas.drawRect(logica.canvasWidth * 0.2f, logica.canvasHeight * 0.6f,
            logica.canvasWidth * 0.8f, logica.canvasHeight * 0.7f, paint)
        paint.color = Color.BLACK
        paint.textSize = 35f
        canvas.drawText("Jugar de Nuevo", logica.canvasWidth / 2f, logica.canvasHeight * 0.65f, paint)
    }
}