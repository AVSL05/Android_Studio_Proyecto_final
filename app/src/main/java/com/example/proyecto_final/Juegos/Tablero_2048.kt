package com.example.proyecto_final.Juegos

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_final.R

class Tablero_2048 : AppCompatActivity() {
    private lateinit var logica: Logica
    private lateinit var guardado2048: Guardado_2048
    private var tamano = 4
    private lateinit var contenedorTablero: LinearLayout
    private lateinit var tvMovimientos: TextView
    private lateinit var btnSalir: Button
    private var tamañoCelda = 60 // Tamaño base en dp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tablero_2048)

        tamano = intent.getIntExtra("TAMANO", 4)
        // Ajustar tamaño de celda según la dificultad
        tamañoCelda = when (tamano) {
            4 -> 60
            5 -> 50
            6 -> 40
            else -> 60
        }

        logica = Logica(tamano)
        guardado2048 = Guardado_2048(this)

        inicializarVistas()
        cargarPartidaGuardada()
        actualizarTablero()
        configurarBotones()
    }

    private fun inicializarVistas() {
        contenedorTablero = findViewById(R.id.contenedorTablero)
        tvMovimientos = findViewById(R.id.tvMovimientos)
        btnSalir = findViewById(R.id.btnSalir)

        // Configurar el layout del tablero
        contenedorTablero.orientation = LinearLayout.VERTICAL
        crearTablero()
    }

    private fun crearTablero() {
        contenedorTablero.removeAllViews()

        for (i in 0 until tamano) {
            val fila = LinearLayout(this)
            fila.orientation = LinearLayout.HORIZONTAL
            fila.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            for (j in 0 until tamano) {
                val celda = layoutInflater.inflate(R.layout.celda_2048, null) as TextView
                celda.layoutParams = LinearLayout.LayoutParams(
                    dpToPx(tamañoCelda),
                    dpToPx(tamañoCelda)
                ).apply {
                    setMargins(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2))
                }

                // Ajustar tamaño de texto según la dificultad
                when (tamano) {
                    4 -> celda.textSize = 18f
                    5 -> celda.textSize = 16f
                    6 -> celda.textSize = 14f
                }

                fila.addView(celda)
            }
            contenedorTablero.addView(fila)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun configurarBotones() {
        val btnArriba: Button = findViewById(R.id.btnArriba)
        val btnAbajo: Button = findViewById(R.id.btnAbajo)
        val btnIzquierda: Button = findViewById(R.id.btnIzquierda)
        val btnDerecha: Button = findViewById(R.id.btnDerecha)

        btnArriba.setOnClickListener { realizarMovimiento(Direccion.ARRIBA) }
        btnAbajo.setOnClickListener { realizarMovimiento(Direccion.ABAJO) }
        btnIzquierda.setOnClickListener { realizarMovimiento(Direccion.IZQUIERDA) }
        btnDerecha.setOnClickListener { realizarMovimiento(Direccion.DERECHA) }

        btnSalir.setOnClickListener { mostrarDialogoSalir() }
    }

    private fun realizarMovimiento(direccion: Direccion) {
        if (logica.mover(direccion)) {
            actualizarTablero()
            guardarPartida()

            if (!logica.isJuegoActivo()) {
                mostrarDialogoFinJuego()
            }
        }
    }

    private fun actualizarTablero() {
        val tablero = logica.getTablero()
        tvMovimientos.text = "Movimientos: ${logica.getMovimientos()}"

        for (i in 0 until tamano) {
            val fila = contenedorTablero.getChildAt(i) as LinearLayout
            for (j in 0 until tamano) {
                val celda = fila.getChildAt(j) as TextView
                val valor = tablero[i][j]

                if (valor == 0) {
                    celda.text = ""
                    celda.setBackgroundColor(Color.parseColor("#CDC1B4"))
                } else {
                    celda.text = valor.toString()
                    celda.setBackgroundColor(obtenerColorParaNumero(valor))
                    celda.setTextColor(if (valor > 4) Color.WHITE else Color.parseColor("#776E65"))
                }
            }
        }
    }

    private fun obtenerColorParaNumero(numero: Int): Int {
        return when (numero) {
            2 -> Color.parseColor("#EEE4DA")
            4 -> Color.parseColor("#EDE0C8")
            8 -> Color.parseColor("#F2B179")
            16 -> Color.parseColor("#F59563")
            32 -> Color.parseColor("#F67C5F")
            64 -> Color.parseColor("#F65E3B")
            128 -> Color.parseColor("#EDCF72")
            256 -> Color.parseColor("#EDCC61")
            512 -> Color.parseColor("#EDC850")
            1024 -> Color.parseColor("#EDC53F")
            2048 -> Color.parseColor("#EDC22E")
            else -> Color.parseColor("#3C3A32")
        }
    }

    private fun mostrarDialogoFinJuego() {
        AlertDialog.Builder(this)
            .setTitle("¡Juego Terminado!")
            .setMessage("Movimientos: ${logica.getMovimientos()}\n\n¿Quieres jugar de nuevo?")
            .setPositiveButton("Sí") { _, _ ->
                reiniciarJuego()
            }
            .setNegativeButton("Salir") { _, _ ->
                guardado2048.limpiarPartida(tamano)
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun mostrarDialogoSalir() {
        AlertDialog.Builder(this)
            .setTitle("Salir del juego")
            .setMessage("¿Estás seguro de que quieres salir? Tu partida se guardará automáticamente.")
            .setPositiveButton("Sí") { _, _ ->
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun guardarPartida() {
        val estado = logica.guardarEstado()
        guardado2048.guardarPartida(tamano, estado)
    }

    private fun cargarPartidaGuardada() {
        val estadoGuardado = guardado2048.cargarPartida(tamano)
        if (estadoGuardado != null) {
            logica.cargarEstado(estadoGuardado)
        }
    }

    private fun reiniciarJuego() {
        logica.reiniciarJuego()
        guardado2048.limpiarPartida(tamano)
        actualizarTablero()
    }

}