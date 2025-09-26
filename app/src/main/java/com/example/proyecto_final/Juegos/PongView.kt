package com.example.proyecto_final.Juegos

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.MotionEvent

class PongView(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    private lateinit var gameThread: GameThread
    private val logicaJuego = LogicaJuego_p(context)
    private val dibujador = Dibujador_p()
    private val controladorToque = ControladorToque_p()

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        logicaJuego.inicializar(width, height)
        gameThread = GameThread(holder)
        gameThread.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        logicaJuego.configurarDimensiones(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        gameThread.detener()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return controladorToque.manejarToque(event, logicaJuego, context)
    }

    private inner class GameThread(private val holder: SurfaceHolder) : Thread() {
        private var running = true

        override fun run() {
            while (running) {
                // Actualizar lógica del juego
                logicaJuego.actualizar()

                // Dibujar en el canvas
                val canvas = holder.lockCanvas()
                if (canvas != null) {
                    try {
                        synchronized(holder) {
                            dibujador.dibujar(canvas, logicaJuego)
                        }
                    } finally {
                        holder.unlockCanvasAndPost(canvas)
                    }
                }

                // Control de FPS (aproximadamente 60 FPS)
                try {
                    sleep(16)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }

        fun detener() {
            running = false
            try {
                join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }
}