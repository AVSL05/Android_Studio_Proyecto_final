package com.example.pong

import kotlin.random.Random
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

class VelocidadPelota_p {

    companion object {
        // VELOCIDAD INICIAL MÁS NOTORIA
        private const val VELOCIDAD_BASE = 13f
        private const val VELOCIDAD_MINIMA = 15f
        private const val VELOCIDAD_MAXIMA = 40f

        // Factores de cambio más significativos
        private const val FACTOR_AUMENTO = 1.3f
        private const val FACTOR_DISMINUCION = 1.0f

        // Ángulos para rebotes aleatorios (en radianes)
        private const val ANGULO_MIN = PI.toFloat() / 6f  // 30 grados
        private const val ANGULO_MAX = PI.toFloat() / 3f  // 60 grados
    }

    private var velocidadX: Float = 0f
    private var velocidadY: Float = 0f
    private var canvasWidth: Int = 0
    private var canvasHeight: Int = 0

    // Control del patrón de velocidad
    private var faseAumento = true
    private var velocidadBaseActual = VELOCIDAD_BASE

    fun inicializar(width: Int, height: Int) {
        this.canvasWidth = width
        this.canvasHeight = height
        resetVelocidad()
    }

    fun resetVelocidad() {
        velocidadBaseActual = VELOCIDAD_BASE
        // Dirección aleatoria pero con ángulo controlado (no demasiado vertical)
        val angulo = Random.nextFloat() * (PI.toFloat() / 2f) + (PI.toFloat() / 4f) // 45-135 grados

        velocidadX = cos(angulo) * velocidadBaseActual
        velocidadY = sin(angulo) * velocidadBaseActual

        // Asegurar dirección inicial variada
        if (Random.nextBoolean()) velocidadX = -velocidadX
        if (Random.nextBoolean()) velocidadY = -velocidadY

        faseAumento = true
    }

    // MÉTODO CORREGIDO: Rebote aleatorio funcional
    fun reboteAleatorio(esReboteVertical: Boolean) {
        if (esReboteVertical) {
            // Rebote vertical (con el enemigo o muro) - ALEATORIO FUNCIONAL
            val anguloAleatorio = Random.nextFloat() * (ANGULO_MAX - ANGULO_MIN) + ANGULO_MIN
            val signoX = if (Random.nextBoolean()) 1f else -1f

            // Usar la velocidad base actual para mantener consistencia
            val velocidadEfectiva = maxOf(velocidadBaseActual, VELOCIDAD_MINIMA)

            velocidadX = signoX * cos(anguloAleatorio) * velocidadEfectiva
            velocidadY = sin(anguloAleatorio) * velocidadEfectiva

            // CORRECCIÓN CRÍTICA: Para enemigo/muro, la pelota debe ir hacia ABAJO (Y positiva)
            velocidadY = abs(velocidadY) // Siempre positiva después de rebote con enemigo/muro

            // Asegurar velocidad mínima
            if (abs(velocidadX) < 3f) velocidadX = if (velocidadX >= 0) 3f else -3f
            if (abs(velocidadY) < 3f) velocidadY = if (velocidadY >= 0) 3f else -3f
        }
    }

    fun cambiarVelocidadAleatoria() {
        val factorCambio = Random.nextFloat() * 0.1f + 0.95f // 0.95 a 1.05

        velocidadX *= factorCambio
        velocidadY *= factorCambio

        ajustarVelocidadALimites()
    }

    fun aumentarDificultadProgresiva() {
        if (faseAumento) {
            velocidadBaseActual *= FACTOR_AUMENTO
            velocidadX *= FACTOR_AUMENTO
            velocidadY *= FACTOR_AUMENTO

            // Limitar velocidad máxima
            if (velocidadBaseActual > VELOCIDAD_MAXIMA) {
                velocidadBaseActual = VELOCIDAD_MAXIMA
            }
        }
        ajustarVelocidadALimites()
    }

    fun disminuirVelocidad() {
        velocidadBaseActual *= FACTOR_DISMINUCION
        velocidadX *= FACTOR_DISMINUCION
        velocidadY *= FACTOR_DISMINUCION
        faseAumento = false

        // Asegurar velocidad mínima
        if (velocidadBaseActual < VELOCIDAD_MINIMA) {
            velocidadBaseActual = VELOCIDAD_MINIMA
        }
        ajustarVelocidadALimites()
    }

    fun continuarProgresion() {
        if (!faseAumento) {
            faseAumento = true
        }
        aumentarDificultadProgresiva()
    }

    private fun ajustarVelocidadALimites() {
        // Limitar velocidad máxima
        val velocidadActual = abs(velocidadX) + abs(velocidadY)
        if (velocidadActual > VELOCIDAD_MAXIMA * 2) {
            val factor = VELOCIDAD_MAXIMA * 2 / velocidadActual
            velocidadX *= factor
            velocidadY *= factor
        }

        // Asegurar velocidad mínima
        if (abs(velocidadX) < 2f) {
            val signo = if (velocidadX >= 0) 1 else -1
            velocidadX = signo * 3f
        }
        if (abs(velocidadY) < 2f) {
            val signo = if (velocidadY >= 0) 1 else -1
            velocidadY = signo * 3f
        }
    }

    fun invertirVelocidadX() {
        velocidadX = -velocidadX
    }

    fun invertirVelocidadY() {
        velocidadY = -velocidadY
    }

    fun getVelocidadX(): Float = velocidadX
    fun getVelocidadY(): Float = velocidadY

    fun setVelocidadY(velY: Float) {
        velocidadY = velY
        ajustarVelocidadALimites()
    }

    fun setVelocidadX(velX: Float) {
        velocidadX = velX
        ajustarVelocidadALimites()
    }

    fun setVelocidadAbsolutaX(velX: Float) {
        val signo = if (velocidadX >= 0) 1 else -1
        velocidadX = abs(velX) * signo
        ajustarVelocidadALimites()
    }

    fun setVelocidadAbsolutaY(velY: Float) {
        val signo = if (velocidadY >= 0) 1 else -1
        velocidadY = abs(velY) * signo
        ajustarVelocidadALimites()
    }

    fun getVelocidadRelativa(): Float {
        return velocidadBaseActual
    }

    fun getPorcentajeVelocidad(): Int {
        return ((velocidadBaseActual - VELOCIDAD_MINIMA) / (VELOCIDAD_MAXIMA - VELOCIDAD_MINIMA) * 100).toInt()
    }

    fun asegurarVelocidadMinimaBase() {
        if (abs(velocidadX) < 3f) {
            val signo = if (velocidadX >= 0) 1 else -1
            velocidadX = signo * 4f
        }
        if (abs(velocidadY) < 3f) {
            val signo = if (velocidadY >= 0) 1 else -1
            velocidadY = signo * 4f
        }
    }

    fun estaEnFaseAumento(): Boolean = faseAumento
}