package com.example.proyecto_final.Juegos

/**
 * Lógica pura del juego Tetris (independiente de Android)
 * - Mantiene tablero, pieza actual y siguiente
 * - Gestiona movimiento, rotación, caída, fijado y limpieza de líneas
 * - Expone callbacks para que la UI reaccione a eventos
 */
class TetrisGame {

    // Dimensiones del tablero en bloques
    val width: Int = 10
    val height: Int = 20

    // Tablero: 0 = vacío, 1..7 = id de color/tipo de pieza
    private val board: Array<IntArray> = Array(height) { IntArray(width) { 0 } }

    // Estado de piezas
    private var current: Piece? = null           // Pieza que cae
    private var next: Piece = randomPiece()      // Siguiente pieza

    // Progreso del juego
    var score: Int = 0
        private set
    var level: Int = 1
        private set
    var linesCleared: Int = 0
        private set

    // Estado general
    var isRunning: Boolean = false
        private set
    var isGameOver: Boolean = false
        private set

    // Listener para notificar a la UI
    var listener: Listener? = null

    /**
     * Eventos que la UI puede escuchar
     */
    interface Listener {
        fun onBoardUpdated() {}            // Solicitar repintado
        fun onPieceLocked() {}             // Una pieza se fijó
        fun onLinesCleared(lines: Int) {}  // Se limpiaron líneas
        fun onGameOver() {}                // Fin del juego
    }

    /**
     * Iniciar o reiniciar partida
     */
    fun start() {
        clearBoard()
        score = 0
        level = 1
        linesCleared = 0
        isGameOver = false
        isRunning = true
        current = null
        spawnNext()
        listener?.onBoardUpdated()
    }

    /**
     * Avanzar un tick: bajar pieza o fijarla si no puede
     */
    fun step() {
        if (!isRunning || isGameOver) return
        val p = current ?: return
        if (canMove(p, p.x, p.y + 1, p.r)) {
            p.y += 1
        } else {
            lockPiece(p)
            val cleared = clearFullLines()
            if (cleared > 0) {
                linesCleared += cleared
                score += cleared * 100 * level
                level = (linesCleared / 10) + 1
                listener?.onLinesCleared(cleared)
            }
            spawnNext()
        }
        listener?.onBoardUpdated()
    }

    /** Mover izquierda */
    fun moveLeft() {
        val p = current ?: return
        if (canMove(p, p.x - 1, p.y, p.r)) {
            p.x -= 1
            listener?.onBoardUpdated()
        }
    }

    /** Mover derecha */
    fun moveRight() {
        val p = current ?: return
        if (canMove(p, p.x + 1, p.y, p.r)) {
            p.x += 1
            listener?.onBoardUpdated()
        }
    }

    /** Caída suave (1 bloque, +1 punto) */
    fun softDrop() {
        val p = current ?: return
        if (canMove(p, p.x, p.y + 1, p.r)) {
            p.y += 1
            score += 1
            listener?.onBoardUpdated()
        }
    }

    /** Caída dura (hasta el fondo, fija y puntúa por distancia) */
    fun hardDrop() {
        val p = current ?: return
        var moved = 0
        while (canMove(p, p.x, p.y + 1, p.r)) {
            p.y += 1
            moved++
        }
        if (moved > 0) score += moved * 2
        lockPiece(p)
        val cleared = clearFullLines()
        if (cleared > 0) {
            linesCleared += cleared
            score += cleared * 100 * level
            level = (linesCleared / 10) + 1
            listener?.onLinesCleared(cleared)
        }
        spawnNext()
        listener?.onBoardUpdated()
    }

    /** Rotar 90º (con pequeños desplazamientos laterales) */
    fun rotate() {
        val p = current ?: return
        val nr = (p.r + 1) % 4
        if (canMove(p, p.x, p.y, nr)) {
            p.r = nr
            listener?.onBoardUpdated()
            return
        }
        if (canMove(p, p.x - 1, p.y, nr)) {
            p.x -= 1; p.r = nr
            listener?.onBoardUpdated(); return
        }
        if (canMove(p, p.x + 1, p.y, nr)) {
            p.x += 1; p.r = nr
            listener?.onBoardUpdated()
        }
    }

    /** Tablero para renderizar: copia + pieza actual superpuesta */
    fun getRenderBoard(): Array<IntArray> {
        val copy = Array(height) { r -> board[r].clone() }
        val p = current ?: return copy
        val shape = p.shape(p.r)
        for (i in shape.indices) {
            for (j in shape[i].indices) {
                if (shape[i][j] == 1) {
                    val bx = p.x + j
                    val by = p.y + i
                    if (by in 0 until height && bx in 0 until width) {
                        if (copy[by][bx] == 0) copy[by][bx] = p.color
                    }
                }
            }
        }
        return copy
    }

    /** Copia del tablero sin pieza actual */
    fun getBoard(): Array<IntArray> = Array(height) { r -> board[r].clone() }

    /** Forma de la siguiente pieza (para preview en UI) */
    fun getNextShape(): Array<IntArray> = next.shape(0)

    // -------------------- Internos --------------------

    // Limpiar tablero
    private fun clearBoard() { for (r in 0 until height) for (c in 0 until width) board[r][c] = 0 }

    // Fijar pieza al tablero
    private fun lockPiece(p: Piece) {
        val shape = p.shape(p.r)
        for (i in shape.indices) for (j in shape[i].indices) if (shape[i][j] == 1) {
            val bx = p.x + j; val by = p.y + i
            if (by in 0 until height && bx in 0 until width) board[by][bx] = p.color
        }
        listener?.onPieceLocked()
    }

    // Limpiar líneas completas y devolver cuántas
    private fun clearFullLines(): Int {
        var cleared = 0
        var r = height - 1
        while (r >= 0) {
            var full = true
            for (c in 0 until width) if (board[r][c] == 0) { full = false; break }
            if (full) {
                for (k in r downTo 1) for (c in 0 until width) board[k][c] = board[k - 1][c]
                for (c in 0 until width) board[0][c] = 0
                cleared++
            } else r--
        }
        return cleared
    }

    // Aparecer siguiente pieza o terminar si no hay espacio
    private fun spawnNext() {
        current = next
        val p = current!!
        val w = p.shape(0)[0].size
        p.x = (width - w) / 2
        p.y = 0
        p.r = 0
        next = randomPiece()
        if (!canMove(p, p.x, p.y, p.r)) {
            isGameOver = true
            isRunning = false
            listener?.onGameOver()
        }
    }

    // Comprobar si una pieza cabe en (nx, ny) con rotación nr
    private fun canMove(p: Piece, nx: Int, ny: Int, nr: Int): Boolean {
        val shape = p.shape(nr)
        for (i in shape.indices) for (j in shape[i].indices) if (shape[i][j] == 1) {
            val bx = nx + j
            val by = ny + i
            if (bx !in 0 until width) return false
            if (by >= height) return false
            if (by >= 0 && board[by][bx] != 0) return false
        }
        return true
    }

    // Seleccionar pieza aleatoria (1..7)
    private fun randomPiece(): Piece = when ((1..7).random()) {
        1 -> Piece.I()
        2 -> Piece.O()
        3 -> Piece.T()
        4 -> Piece.S()
        5 -> Piece.Z()
        6 -> Piece.J()
        else -> Piece.L()
    }

    /**
     * Pieza de Tetris con sus rotaciones
     */
    class Piece(
        var x: Int = 0,                 // posición X
        var y: Int = 0,                 // posición Y
        var r: Int = 0,                 // rotación 0..3
        val color: Int,                 // id de color 1..7
        private val shapes: Array<Array<IntArray>> // matrices por rotación
    ) {
        fun shape(rotation: Int): Array<IntArray> = shapes[rotation % shapes.size]

        companion object {
            fun I() = Piece(
                color = 1,
                shapes = arrayOf(
                    arrayOf(intArrayOf(1, 1, 1, 1)),
                    arrayOf(intArrayOf(1), intArrayOf(1), intArrayOf(1), intArrayOf(1)),
                    arrayOf(intArrayOf(1, 1, 1, 1)),
                    arrayOf(intArrayOf(1), intArrayOf(1), intArrayOf(1), intArrayOf(1))
                )
            )
            fun O() = Piece(
                color = 2,
                shapes = arrayOf(
                    arrayOf(intArrayOf(1, 1), intArrayOf(1, 1)),
                    arrayOf(intArrayOf(1, 1), intArrayOf(1, 1)),
                    arrayOf(intArrayOf(1, 1), intArrayOf(1, 1)),
                    arrayOf(intArrayOf(1, 1), intArrayOf(1, 1))
                )
            )
            fun T() = Piece(
                color = 3,
                shapes = arrayOf(
                    arrayOf(intArrayOf(0, 1, 0), intArrayOf(1, 1, 1)),
                    arrayOf(intArrayOf(1, 0), intArrayOf(1, 1), intArrayOf(1, 0)),
                    arrayOf(intArrayOf(1, 1, 1), intArrayOf(0, 1, 0)),
                    arrayOf(intArrayOf(0, 1), intArrayOf(1, 1), intArrayOf(0, 1))
                )
            )
            fun S() = Piece(
                color = 4,
                shapes = arrayOf(
                    arrayOf(intArrayOf(0, 1, 1), intArrayOf(1, 1, 0)),
                    arrayOf(intArrayOf(1, 0), intArrayOf(1, 1), intArrayOf(0, 1)),
                    arrayOf(intArrayOf(0, 1, 1), intArrayOf(1, 1, 0)),
                    arrayOf(intArrayOf(1, 0), intArrayOf(1, 1), intArrayOf(0, 1))
                )
            )
            fun Z() = Piece(
                color = 5,
                shapes = arrayOf(
                    arrayOf(intArrayOf(1, 1, 0), intArrayOf(0, 1, 1)),
                    arrayOf(intArrayOf(0, 1), intArrayOf(1, 1), intArrayOf(1, 0)),
                    arrayOf(intArrayOf(1, 1, 0), intArrayOf(0, 1, 1)),
                    arrayOf(intArrayOf(0, 1), intArrayOf(1, 1), intArrayOf(1, 0))
                )
            )
            fun J() = Piece(
                color = 6,
                shapes = arrayOf(
                    arrayOf(intArrayOf(1, 0, 0), intArrayOf(1, 1, 1)),
                    arrayOf(intArrayOf(1, 1), intArrayOf(1, 0), intArrayOf(1, 0)),
                    arrayOf(intArrayOf(1, 1, 1), intArrayOf(0, 0, 1)),
                    arrayOf(intArrayOf(0, 1), intArrayOf(0, 1), intArrayOf(1, 1))
                )
            )
            fun L() = Piece(
                color = 7,
                shapes = arrayOf(
                    arrayOf(intArrayOf(0, 0, 1), intArrayOf(1, 1, 1)),
                    arrayOf(intArrayOf(1, 0), intArrayOf(1, 0), intArrayOf(1, 1)),
                    arrayOf(intArrayOf(1, 1, 1), intArrayOf(1, 0, 0)),
                    arrayOf(intArrayOf(1, 1), intArrayOf(0, 1), intArrayOf(0, 1))
                )
            )
        }
    }
}

