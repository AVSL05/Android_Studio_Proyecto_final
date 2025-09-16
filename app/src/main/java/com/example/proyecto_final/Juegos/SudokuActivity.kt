package com.example.proyecto_final.Juegos

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_final.MainActivity
import com.example.proyecto_final.R

class SudokuActivity : AppCompatActivity() {

    private lateinit var gridLayout: GridLayout
    private lateinit var numberButtons: LinearLayout
    private lateinit var tvLives: TextView
    private lateinit var btnHint: Button
    private lateinit var btnExit: Button

    private val sudokuGrid = Array(9) { Array(9) { 0 } }
    private val initialGrid = Array(9) { Array(9) { 0 } }
    private val solutionGrid = Array(9) { Array(9) { 0 } }
    private val cellButtons = Array(9) { Array<Button?>(9) { null } }

    private var selectedRow = -1
    private var selectedCol = -1
    private var lives = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sudoku)

        initViews()
        generateSudoku()
        setupGrid()
        setupNumberButtons()
        setupHintButton()
        setupExitButton()
        updateLivesDisplay()
    }

    private fun initViews() {
        gridLayout = findViewById(R.id.gridSudoku)
        numberButtons = findViewById(R.id.numberButtons)
        tvLives = findViewById(R.id.tvLives)
        btnHint = findViewById(R.id.btnHint)
        btnExit = findViewById(R.id.btnExit)
    }

    private fun generateSudoku() {
        // Soluciones de ejemplo
        val solutions = arrayOf(
            arrayOf(
                intArrayOf(5,3,4,6,7,8,9,1,2),
                intArrayOf(6,7,2,1,9,5,3,4,8),
                intArrayOf(1,9,8,3,4,2,5,6,7),
                intArrayOf(8,5,9,7,6,1,4,2,3),
                intArrayOf(4,2,6,8,5,3,7,9,1),
                intArrayOf(7,1,3,9,2,4,8,5,6),
                intArrayOf(9,6,1,5,3,7,2,8,4),
                intArrayOf(2,8,7,4,1,9,6,3,5),
                intArrayOf(3,4,5,2,8,6,1,7,9)
            ),
            arrayOf(
                intArrayOf(1,2,3,4,5,6,7,8,9),
                intArrayOf(4,5,6,7,8,9,1,2,3),
                intArrayOf(7,8,9,1,2,3,4,5,6),
                intArrayOf(2,3,1,5,6,4,8,9,7),
                intArrayOf(5,6,4,8,9,7,2,3,1),
                intArrayOf(8,9,7,2,3,1,5,6,4),
                intArrayOf(3,1,2,6,4,5,9,7,8),
                intArrayOf(6,4,5,9,7,8,3,1,2),
                intArrayOf(9,7,8,3,1,2,6,4,5)
            )
        )

        val randomSolution = solutions.random()
        for (i in 0..8) for (j in 0..8) solutionGrid[i][j] = randomSolution[i][j]

        // Copiar solución y crear puzzle eliminando celdas
        val puzzle = Array(9) { Array(9) { 0 } }
        for (i in 0..8) for (j in 0..8) puzzle[i][j] = solutionGrid[i][j]

        val allCells = mutableListOf<Pair<Int, Int>>()
        for (i in 0..8) for (j in 0..8) allCells.add(Pair(i, j))
        allCells.shuffle()

        val cellsToRemove = (40..50).random()
        for (k in 0 until cellsToRemove) {
            val (r, c) = allCells[k]
            puzzle[r][c] = 0
        }

        // Asegurar mínimo visible en cada subcuadrante
        for (boxRow in 0..2) {
            for (boxCol in 0..2) {
                var visible = 0
                for (i in boxRow * 3 until boxRow * 3 + 3) {
                    for (j in boxCol * 3 until boxCol * 3 + 3) {
                        if (puzzle[i][j] != 0) visible++
                    }
                }
                if (visible < 2) {
                    for (i in boxRow * 3 until boxRow * 3 + 3) {
                        for (j in boxCol * 3 until boxCol * 3 + 3) {
                            if (puzzle[i][j] == 0 && visible < 3) {
                                puzzle[i][j] = solutionGrid[i][j]
                                visible++
                            }
                        }
                    }
                }
            }
        }

        for (i in 0..8) for (j in 0..8) {
            sudokuGrid[i][j] = puzzle[i][j]
            initialGrid[i][j] = puzzle[i][j]
        }
    }

    private fun setupGrid() {
        gridLayout.removeAllViews()
        gridLayout.rowCount = 9
        gridLayout.columnCount = 9

        for (i in 0..8) {
            for (j in 0..8) {
                val button = Button(this)
                button.layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 0
                    rowSpec = GridLayout.spec(i, 1f)
                    columnSpec = GridLayout.spec(j, 1f)
                    setMargins(2, 2, 2, 2)
                }

                if (initialGrid[i][j] != 0) {
                    button.text = initialGrid[i][j].toString()
                    button.setBackgroundColor(Color.LTGRAY)
                    button.isEnabled = false
                } else {
                    button.text = ""
                    button.setBackgroundColor(Color.WHITE)
                }

                button.textSize = 18f
                button.setTextColor(Color.BLACK)

                if (i % 3 == 2 && i != 8) button.setPadding(0, 0, 0, 4)
                if (j % 3 == 2 && j != 8) button.setPadding(0, 0, 4, 0)

                val row = i
                val col = j
                button.setOnClickListener { selectCell(row, col) }

                cellButtons[i][j] = button
                gridLayout.addView(button)
            }
        }
    }

    private fun setupNumberButtons() {
        numberButtons.removeAllViews()
        for (num in 1..9) {
            val button = Button(this)
            button.text = num.toString()
            button.textSize = 20f
            button.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply { setMargins(4, 4, 4, 4) }

            button.setOnClickListener {
                if (selectedRow != -1 && selectedCol != -1) placeNumber(num)
            }

            numberButtons.addView(button)
        }

        val clearButton = Button(this)
        clearButton.text = getString(R.string.clear)
        clearButton.textSize = 16f
        clearButton.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        ).apply { setMargins(4, 4, 4, 4) }

        clearButton.setOnClickListener {
            if (selectedRow != -1 && selectedCol != -1) placeNumber(0)
        }

        numberButtons.addView(clearButton)
    }

    private fun setupHintButton() {
        btnHint.setOnClickListener { giveHint() }
    }

    private fun giveHint() {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (i in 0..8) for (j in 0..8) if (sudokuGrid[i][j] == 0) emptyCells.add(Pair(i, j))

        if (emptyCells.isEmpty()) {
            Toast.makeText(this, "¡No hay más celdas vacías!", Toast.LENGTH_SHORT).show()
            return
        }

        val (row, col) = emptyCells.random()
        val correctNumber = solutionGrid[row][col]

        sudokuGrid[row][col] = correctNumber
        cellButtons[row][col]?.text = correctNumber.toString()
        cellButtons[row][col]?.setTextColor(Color.BLUE)

        Toast.makeText(this, "¡Pista agregada!", Toast.LENGTH_SHORT).show()

        if (isPuzzleComplete()) showWinDialog()
    }

    private fun selectCell(row: Int, col: Int) {
        if (selectedRow != -1 && selectedCol != -1) {
            val prev = cellButtons[selectedRow][selectedCol]
            if (initialGrid[selectedRow][selectedCol] != 0) prev?.setBackgroundColor(Color.LTGRAY)
            else prev?.setBackgroundColor(Color.WHITE)
        }

        if (initialGrid[row][col] == 0) {
            selectedRow = row
            selectedCol = col
            cellButtons[row][col]?.setBackgroundColor(Color.CYAN)
        }
    }

    private fun placeNumber(num: Int) {
        if (selectedRow == -1 || selectedCol == -1) return

        sudokuGrid[selectedRow][selectedCol] = num
        cellButtons[selectedRow][selectedCol]?.text = if (num == 0) "" else num.toString()

        if (num != 0) {
            val correctNumber = solutionGrid[selectedRow][selectedCol]
            if (num == correctNumber) cellButtons[selectedRow][selectedCol]?.setTextColor(Color.BLACK)
            else {
                cellButtons[selectedRow][selectedCol]?.setTextColor(Color.RED)
                lives--
                updateLivesDisplay()
                if (lives <= 0) { showGameOverDialog(); return }
            }
        }

        if (isPuzzleComplete()) showWinDialog()
    }

    private fun updateLivesDisplay() {
        val heartsText = when (lives) {
            3 -> "Vidas: ❤️❤️❤️"
            2 -> "Vidas: ❤️❤️🖤"
            1 -> "Vidas: ❤️🖤🖤"
            0 -> "Vidas: 🖤🖤🖤"
            else -> "Vidas: ❤️❤️❤️"
        }
        tvLives.text = heartsText
    }

    private fun isPuzzleComplete(): Boolean {
        for (i in 0..8) for (j in 0..8) if (sudokuGrid[i][j] != solutionGrid[i][j]) return false
        return true
    }

    private fun showGameOverDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("¡Game Over!")
        builder.setMessage("Te quedaste sin vidas.\n\n¿Qué quieres hacer?")
        builder.setCancelable(false)
        builder.setPositiveButton("Reiniciar") { dialog, _ -> dialog.dismiss(); recreate() }
        builder.setNeutralButton("Menú Principal") { dialog, _ ->
            dialog.dismiss()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        builder.create().show()
    }

    private fun showWinDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("¡Felicidades!")
        builder.setMessage("¡Has completado el Sudoku!\n\n¿Qué quieres hacer ahora?")
        builder.setCancelable(false)
        builder.setPositiveButton("Reiniciar") { dialog, _ -> dialog.dismiss(); recreate() }
        builder.setNeutralButton("Menú Principal") { dialog, _ ->
            dialog.dismiss()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        builder.create().show()
    }

    private fun setupExitButton() {
            Log.d("SudokuActivity", "btnExit clicked - showing confirmation dialog")
            Toast.makeText(this, "Preparando salida...", Toast.LENGTH_SHORT).show()

        btnExit.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Salir")
            builder.setMessage("¿Estás seguro de que quieres salir del juego?")
                Log.d("SudokuActivity", "Salir confirmado - launching MainActivity and finishing")
                // Aseguramos volver al menú principal de forma consistente
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            builder.setPositiveButton("Sí") { dialog, which ->
                finish()
            }
                Log.d("SudokuActivity", "Salir cancelado")
            builder.setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            builder.show()
        }
    }
}
