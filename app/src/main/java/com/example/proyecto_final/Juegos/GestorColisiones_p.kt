package com.example.pong

import kotlin.math.abs

class GestorColisiones_p(
    private val logica: LogicaJuego_p,
    private val gestorVelocidad: VelocidadPelota_p
) {
    private var ultimoReboteEnemigo = 0L
    private var rebotesConsecutivos = 0
    private val tiempoMinimoEntreRebotes = 100L

    private var ultimoRebotePared = 0L
    private val tiempoMinimoEntreRebotesPared = 50L

    fun manejarRebotesParedes() {
        val tiempoActual = System.currentTimeMillis()

        // Rebotes en bordes laterales - MÁS RESPONSIVO
        if (logica.ballX - logica.ballRadius < 0) {
            if (tiempoActual - ultimoRebotePared > tiempoMinimoEntreRebotesPared) {
                gestorVelocidad.invertirVelocidadX()
                logica.ballX = logica.ballRadius + 5f // Mayor margen
                ultimoRebotePared = tiempoActual
            }
        } else if (logica.ballX + logica.ballRadius > logica.canvasWidth) {
            if (tiempoActual - ultimoRebotePared > tiempoMinimoEntreRebotesPared) {
                gestorVelocidad.invertirVelocidadX()
                logica.ballX = logica.canvasWidth - logica.ballRadius - 5f
                ultimoRebotePared = tiempoActual
            }
        }
    }

    fun manejarColisiones() {
        val tiempoActual = System.currentTimeMillis()

        // Colisión con paleta del enemigo (arriba) - CORREGIDO
        if (logica.ballY - logica.ballRadius < logica.enemyPaddleHeight &&
            logica.ballX >= logica.enemyPaddleX - 10 && logica.ballX <= logica.enemyPaddleX + logica.enemyPaddleWidth + 10) {

            if (tiempoActual - ultimoReboteEnemigo > tiempoMinimoEntreRebotes) {
                rebotesConsecutivos = 0
                ultimoReboteEnemigo = tiempoActual

                // REBOTE ALEATORIO FUNCIONAL
                gestorVelocidad.reboteAleatorio(true)

                // Asegurar dirección correcta (hacia abajo)
                gestorVelocidad.setVelocidadAbsolutaY(abs(gestorVelocidad.getVelocidadY()))

                // Posicionar la pelota debajo del enemigo
                logica.ballY = logica.enemyPaddleHeight + logica.ballRadius + 5f

                // Cambio de velocidad más notable
                gestorVelocidad.cambiarVelocidadAleatoria()

                logica.bounceCount++
                logica.ajustarDificultad()

                // DEBUG: Mostrar en consola
                println("REBOTE ENEMIGO - Velocidad: ${gestorVelocidad.getVelocidadRelativa()}")
            }
        }

        // "Muro" detrás del enemigo - CORREGIDO
        if (logica.ballY - logica.ballRadius < 0) {
            if (tiempoActual - ultimoReboteEnemigo > tiempoMinimoEntreRebotes) {

                // REBOTE ALEATORIO desde el muro
                gestorVelocidad.reboteAleatorio(true)

                // Asegurar dirección hacia abajo
                gestorVelocidad.setVelocidadAbsolutaY(abs(gestorVelocidad.getVelocidadY()))

                // Posicionar la pelota debajo del muro
                logica.ballY = logica.ballRadius + 5f

                // +3 PUNTOS y aumento de velocidad
                logica.score += 3
                gestorVelocidad.cambiarVelocidadAleatoria()
                logica.bounceCount++
                logica.ajustarDificultad()
                rebotesConsecutivos = 0

                // DEBUG
                println("REBOTE MURO - +3 PUNTOS - Velocidad: ${gestorVelocidad.getVelocidadRelativa()}")
            }
        }

        // Colisión con paleta del jugador (abajo) - MEJORADO
        if (logica.ballY + logica.ballRadius > logica.canvasHeight - logica.paddleHeight &&
            logica.ballX >= logica.paddleX - 10 && logica.ballX <= logica.paddleX + logica.paddleWidth + 10) {

            // Rebote controlado para el jugador
            gestorVelocidad.invertirVelocidadY()
            gestorVelocidad.setVelocidadAbsolutaY(-abs(gestorVelocidad.getVelocidadY()))

            // Asegurar posición
            if (logica.ballY > logica.canvasHeight - logica.paddleHeight - logica.ballRadius) {
                logica.ballY = logica.canvasHeight - logica.paddleHeight - logica.ballRadius - 5f
            }

            // Aumento de velocidad más significativo
            gestorVelocidad.cambiarVelocidadAleatoria()
            gestorVelocidad.asegurarVelocidadMinimaBase()

            logica.score++
            logica.bounceCount++
            logica.ajustarDificultad()
            rebotesConsecutivos = 0

            // DEBUG
            println("REBOTE JUGADOR - Puntos: ${logica.score} - Velocidad: ${gestorVelocidad.getVelocidadRelativa()}")
        }
    }

    fun reset() {
        ultimoReboteEnemigo = 0L
        ultimoRebotePared = 0L
        rebotesConsecutivos = 0
    }
}