package com.example.proyecto_final

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.MotionEvent

class PongView(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    private lateinit var gameThread: GameThread_p
    private val logicaJuego_p = LogicaJuego_p(context)
    private val dibujador_p = Dibujador_p()
    private val controladorToque_p = ControladorToque_p()

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        logicaJuego_p.inicializar(width, height)
        gameThread = GameThread_p(holder)
        gameThread.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        logicaJuego_p.configurarDimensiones(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var retry = true
        gameThread.running = false
        while (retry) {
            try {
                gameThread.join()
                retry = false
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return controladorToque_p.manejarToque(event, logicaJuego_p, context)
    }

    private inner class GameThread_p(private val holder: SurfaceHolder) : Thread() {
        var running = true

        override fun run() {
            while (running) {
                logicaJuego_p.actualizar()

                val canvas = holder.lockCanvas()
                if (canvas != null) {
                    try {
                        dibujador_p.dibujar(canvas, logicaJuego_p)
                    } finally {
                        holder.unlockCanvasAndPost(canvas)
                    }
                }

                try {
                    sleep(16)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }
}