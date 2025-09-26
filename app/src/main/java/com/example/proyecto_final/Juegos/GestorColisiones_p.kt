package com.example.proyecto_final

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
        if (logica.pelotaX - logica.pelotaRadio < 0) {
            if (tiempoActual - ultimoRebotePared > tiempoMinimoEntreRebotesPared) {
                gestorVelocidad.invertirVelocidadX()
                logica.pelotaX = logica.pelotaRadio + 5f
                ultimoRebotePared = tiempoActual
            }
        } else if (logica.pelotaX + logica.pelotaRadio > logica.anchoPantalla) {
            if (tiempoActual - ultimoRebotePared > tiempoMinimoEntreRebotesPared) {
                gestorVelocidad.invertirVelocidadX()
                logica.pelotaX = logica.anchoPantalla - logica.pelotaRadio - 5f
                ultimoRebotePared = tiempoActual
            }
        }
    }

    fun manejarColisiones() {
        val tiempoActual = System.currentTimeMillis()

        // Colisión con paleta de la IA (arriba)
        if (logica.pelotaY - logica.pelotaRadio < logica.paletaIAY + logica.paletaAlto &&
            logica.pelotaX >= logica.paletaIAX - 10 && logica.pelotaX <= logica.paletaIAX + logica.paletaAncho + 10) {

            if (tiempoActual - ultimoReboteEnemigo > tiempoMinimoEntreRebotes) {
                rebotesConsecutivos = 0
                ultimoReboteEnemigo = tiempoActual

                // REBOTE ALEATORIO FUNCIONAL
                gestorVelocidad.reboteAleatorio(true)

                // Asegurar dirección correcta (hacia abajo)
                gestorVelocidad.setVelocidadAbsolutaY(abs(gestorVelocidad.getVelocidadY()))

                // Posicionar la pelota debajo de la paleta IA
                logica.pelotaY = logica.paletaIAY + logica.paletaAlto + logica.pelotaRadio + 5f

                // Cambio de velocidad más notable
                gestorVelocidad.cambiarVelocidadAleatoria()

                logica.puntajeJugador++

                // DEBUG: Mostrar en consola
                println("REBOTE IA - Velocidad: ${gestorVelocidad.getVelocidadRelativa()}")
            }
        }

        // "Muro" detrás de la IA - CORREGIDO
        if (logica.pelotaY - logica.pelotaRadio < 0) {
            if (tiempoActual - ultimoReboteEnemigo > tiempoMinimoEntreRebotes) {

                // REBOTE ALEATORIO desde el muro
                gestorVelocidad.reboteAleatorio(true)

                // Asegurar dirección hacia abajo
                gestorVelocidad.setVelocidadAbsolutaY(abs(gestorVelocidad.getVelocidadY()))

                // Posicionar la pelota debajo del muro
                logica.pelotaY = logica.pelotaRadio + 5f

                // +3 PUNTOS y aumento de velocidad
                logica.puntajeJugador += 3
                gestorVelocidad.cambiarVelocidadAleatoria()
                rebotesConsecutivos = 0

                // DEBUG
                println("REBOTE MURO - +3 PUNTOS - Velocidad: ${gestorVelocidad.getVelocidadRelativa()}")
            }
        }

        // Colisión con paleta del jugador (abajo) - MEJORADO
        if (logica.pelotaY + logica.pelotaRadio > logica.paletaJugadorY &&
            logica.pelotaX >= logica.paletaJugadorX - 10 && logica.pelotaX <= logica.paletaJugadorX + logica.paletaAncho + 10) {

            // Rebote controlado para el jugador
            gestorVelocidad.invertirVelocidadY()
            gestorVelocidad.setVelocidadAbsolutaY(-abs(gestorVelocidad.getVelocidadY()))

            // Asegurar posición
            if (logica.pelotaY > logica.paletaJugadorY - logica.pelotaRadio) {
                logica.pelotaY = logica.paletaJugadorY - logica.pelotaRadio - 5f
            }

            // Aumento de velocidad más significativo
            gestorVelocidad.cambiarVelocidadAleatoria()
            gestorVelocidad.asegurarVelocidadMinimaBase()

            logica.puntajeJugador++
            rebotesConsecutivos = 0

            // DEBUG
            println("REBOTE JUGADOR - Puntos: ${logica.puntajeJugador} - Velocidad: ${gestorVelocidad.getVelocidadRelativa()}")
        }
    }

    fun reset() {
        ultimoReboteEnemigo = 0L
        ultimoRebotePared = 0L
        rebotesConsecutivos = 0
    }
}