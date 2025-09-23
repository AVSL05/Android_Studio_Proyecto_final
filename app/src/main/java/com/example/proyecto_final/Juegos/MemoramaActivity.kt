package com.example.proyecto_final.Juegos

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_final.R
import com.example.proyecto_final.MainActivity

class MemoramaActivity : AppCompatActivity() {
    data class Card(
        val id: Int,
        val imageResId: Int,
        var isFlipped: Boolean = false,
        var isMatched: Boolean = false
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memorama)

        // Configurar botón de salir
        val btnExitMemorama = findViewById<Button>(R.id.btnExitMemorama)
        btnExitMemorama.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // Configurar el juego
        val gridLayout = findViewById<GridLayout>(R.id.gridMemorama)
        val numPairs = 8
        val images = listOf(
            R.drawable.cactus, R.drawable.papapum,
            R.drawable.girasol, R.drawable.planta_carnivora,
            R.drawable.lanza_guisantes, R.drawable.pvzlogo,
            R.drawable.melonpulta, R.drawable.zombie
        )
        // Asegurarse de tener suficientes imágenes
        val cards = (images + images).shuffled().mapIndexed { index, imageResId ->
            Card(id = index, imageResId = imageResId)
        }

        // Variables para el estado del juego
        var firstFlipped: Button? = null
        var firstCard: Card? = null
        var matchedPairs = 0

        // Configurar la cuadrícula
        gridLayout.removeAllViews()
        gridLayout.rowCount = 4
        gridLayout.columnCount = 4

        // Crear botones para las cartas
        val buttons = mutableListOf<Button>()
        for (i in 0 until 16) {
            val btn = Button(this)
            btn.layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = 0
                rowSpec = GridLayout.spec(i / 4, 1f)
                columnSpec = GridLayout.spec(i % 4, 1f)
            }
            btn.text = "?"
            btn.textSize = 24f
            btn.setOnClickListener {
                val card = cards[i]
                if (card.isFlipped || card.isMatched) return@setOnClickListener
                btn.setBackgroundResource(card.imageResId)
                btn.text = ""
                card.isFlipped = true
                if (firstFlipped == null) {
                    firstFlipped = btn
                    firstCard = card
                } else {
                    // Deshabilitar clicks mientras se compara
                    gridLayout.isEnabled = false
                    btn.postDelayed({
                        if (card.imageResId == firstCard?.imageResId) {
                            card.isMatched = true
                            firstCard?.isMatched = true
                            matchedPairs++
                            if (matchedPairs == numPairs) {
                                showWinDialog()
                            }
                            // Dejar las cartas volteadas
                        } else {
                            btn.setBackgroundResource(0)
                            btn.text = "?"
                            firstFlipped?.setBackgroundResource(0)
                            firstFlipped?.text = "?"
                            card.isFlipped = false
                            firstCard?.isFlipped = false
                        }
                        // Resetear para la siguiente jugada
                        firstFlipped = null
                        firstCard = null
                        gridLayout.isEnabled = true
                    }, 500)
                }
            }
            gridLayout.addView(btn)
            buttons.add(btn)
        }
    }

    // Mostrar diálogo de victoria
    private fun showWinDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("¡Felicidades!")
        builder.setMessage("¡Has ganado el memorama!\n\n¿Qué quieres hacer ahora?")
        builder.setCancelable(false) // No permitir cerrar tocando fuera del diálogo

        // Botón para reiniciar el juego
        builder.setPositiveButton("Reiniciar") { dialog, _ ->
            dialog.dismiss()
            recreate() // Reinicia la actividad
        }

        // Botón para volver al menú principal
        builder.setNeutralButton("Menú Principal") { dialog, _ ->
            dialog.dismiss()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // Botón para salir de la aplicación
        val dialog = builder.create()
        dialog.show()
    }
}
