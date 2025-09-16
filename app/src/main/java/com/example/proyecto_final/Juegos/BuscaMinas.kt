package com.example.proyecto_final.Juegos

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.CheckBox
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random
import com.example.proyecto_final.R

class Buscaminas : AppCompatActivity() {

    private lateinit var gridLayout: GridLayout
    private lateinit var tvMines: TextView
    private lateinit var btnNewGame: Button
    private lateinit var cbFlagMode: CheckBox
    private lateinit var btnExitBuscaminas: Button

    private val rows = 9
    private val cols = 9
    private val totalMines = 10

    private lateinit var isMine: Array<BooleanArray>
    private lateinit var adj: Array<IntArray>
    private lateinit var revealed: Array<BooleanArray>
    private lateinit var flagged: Array<BooleanArray>
    private lateinit var buttons: Array<Array<Button>>

    private var flagsPlaced = 0
    private var gameOver = false
    private var firstClick = true // para evitar perder en el primer toque

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buscaminas)

        gridLayout = findViewById(R.id.grid)
        tvMines = findViewById(R.id.tvMines)
        btnNewGame = findViewById(R.id.btnNewGame)
        cbFlagMode = findViewById(R.id.cbFlagMode)
        btnExitBuscaminas = findViewById(R.id.btnExitBuscaminas)

        btnNewGame.setOnClickListener { newGame() }
        cbFlagMode.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this,
                if (isChecked) "Modo bandera ON" else "Modo bandera OFF",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Botón salir
        val btnExitBuscaminas = findViewById<Button>(R.id.btnExitBuscaminas)
        btnExitBuscaminas.setOnClickListener {
            val intent = Intent(this, Buscaminas::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        setupBoardViews()
        newGame()
    }

    private fun setupBoardViews() {
        buttons = Array(rows) { r ->
            Array(cols) { c ->
                Button(this).apply {
                    val sizePx = dp(36)
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = sizePx
                        height = sizePx
                        rowSpec = GridLayout.spec(r)
                        columnSpec = GridLayout.spec(c)
                    }
                    gravity = Gravity.CENTER
                    textSize = 16f
                    typeface = Typeface.DEFAULT_BOLD

                    setOnClickListener {
                        if (cbFlagMode.isChecked) {
                            toggleFlag(r, c)
                        } else {
                            onCellClick(r, c)
                        }
                    }
                    setOnLongClickListener {
                        toggleFlag(r, c)
                        true
                    }
                    gridLayout.addView(this)
                }
            }
        }
    }

    private fun newGame() {
        gameOver = false
        firstClick = true
        flagsPlaced = 0
        cbFlagMode.isChecked = false

        isMine = Array(rows) { BooleanArray(cols) }
        adj = Array(rows) { IntArray(cols) }
        revealed = Array(rows) { BooleanArray(cols) }
        flagged = Array(rows) { BooleanArray(cols) }

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val b = buttons[r][c]
                b.isEnabled = true
                b.text = ""
                b.isSelected = false
            }
        }
        updateMinesCounter()
    }

    private fun placeMines(excludeR: Int, excludeC: Int) {
        var placed = 0
        while (placed < totalMines) {
            val r = Random.nextInt(rows)
            val c = Random.nextInt(cols)
            if ((r == excludeR && c == excludeC) || isMine[r][c]) continue
            isMine[r][c] = true
            placed++
        }
        computeAdj()
    }

    private fun computeAdj() {
        val dirs = arrayOf(
            -1 to -1, -1 to 0, -1 to 1,
            0 to -1,          0 to 1,
            1 to -1,  1 to 0, 1 to 1
        )
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (isMine[r][c]) {
                    adj[r][c] = -1
                } else {
                    var count = 0
                    for ((dr, dc) in dirs) {
                        val nr = r + dr
                        val nc = c + dc
                        if (nr in 0 until rows && nc in 0 until cols && isMine[nr][nc]) count++
                    }
                    adj[r][c] = count
                }
            }
        }
    }

    private fun onCellClick(r: Int, c: Int) {
        if (gameOver) return
        if (flagged[r][c]) return

        if (firstClick) {
            firstClick = false
            placeMines(excludeR = r, excludeC = c)
        }

        reveal(r, c)

        if (!gameOver && checkWin()) {
            gameOver = true
            disableAll()
            Toast.makeText(this, "🎉 ¡Has ganado!", Toast.LENGTH_LONG).show()
        }
    }

    private fun toggleFlag(r: Int, c: Int) {
        if (gameOver) return
        if (revealed[r][c]) return

        flagged[r][c] = !flagged[r][c]
        val b = buttons[r][c]
        if (flagged[r][c]) {
            b.text = "🚩"
            flagsPlaced++
        } else {
            b.text = ""
            flagsPlaced--
        }
        updateMinesCounter()
    }

    private fun reveal(sr: Int, sc: Int) {
        if (revealed[sr][sc] || flagged[sr][sc]) return

        if (isMine[sr][sc]) {
            showAllMines()
            gameOver = true
            disableAll()
            Toast.makeText(this, "💥 ¡Pisaste una mina!", Toast.LENGTH_LONG).show()
            return
        }

        val queue = ArrayDeque<Pair<Int, Int>>()
        queue.add(sr to sc)
        val seen = HashSet<Pair<Int, Int>>()

        while (queue.isNotEmpty()) {
            val (r, c) = queue.removeFirst()
            if ((r to c) in seen) continue
            seen.add(r to c)

            if (!revealed[r][c]) {
                revealed[r][c] = true
                val b = buttons[r][c]
                b.isEnabled = false

                val n = adj[r][c]
                if (n > 0) {
                    b.text = n.toString()
                    setNumberColor(b, n)
                } else {
                    b.text = ""
                    for (nr in (r - 1)..(r + 1)) {
                        for (nc in (c - 1)..(c + 1)) {
                            if (nr == r && nc == c) continue
                            if (nr in 0 until rows && nc in 0 until cols && !isMine[nr][nc] && !revealed[nr][nc]) {
                                queue.add(nr to nc)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setNumberColor(b: Button, n: Int) {
        val color = when (n) {
            1 -> 0xFF1565C0.toInt()
            2 -> 0xFF2E7D32.toInt()
            3 -> 0xFFC62828.toInt()
            4 -> 0xFF4527A0.toInt()
            5 -> 0xFF6D4C41.toInt()
            6 -> 0xFF00838F.toInt()
            7 -> 0xFF424242.toInt()
            else -> 0xFF000000.toInt()
        }
        b.setTextColor(color)
    }

    private fun showAllMines() {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (isMine[r][c]) {
                    val b = buttons[r][c]
                    b.text = "💣"
                    b.isEnabled = false
                }
            }
        }
    }

    private fun disableAll() {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                buttons[r][c].isEnabled = false
            }
        }
    }

    private fun checkWin(): Boolean {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (!isMine[r][c] && !revealed[r][c]) return false
            }
        }
        return true
    }

    private fun updateMinesCounter() {
        val remaining = (totalMines - flagsPlaced).coerceAtLeast(0)
        tvMines.text = "💣 $remaining"
    }

    private fun dp(value: Int): Int {
        val scale = resources.displayMetrics.density
        return (value * scale).toInt()
    }
}
