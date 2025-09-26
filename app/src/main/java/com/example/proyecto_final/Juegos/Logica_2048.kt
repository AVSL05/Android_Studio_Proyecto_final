package com.example.a2048

class Logica(private val tamano: Int) {
    private var tablero = Array(tamano) { IntArray(tamano) }
    private var movimientos = 0
    private var juegoActivo = true

    init {
        reiniciarJuego()
    }

    fun reiniciarJuego() {
        tablero = Array(tamano) { IntArray(tamano) }
        movimientos = 0
        juegoActivo = true
        agregarNumeroAleatorio()
        agregarNumeroAleatorio()
    }

    fun getTablero(): Array<IntArray> = tablero

    fun getMovimientos(): Int = movimientos

    fun isJuegoActivo(): Boolean = juegoActivo

    fun mover(direccion: Direccion): Boolean {
        if (!juegoActivo) return false

        val tableroAnterior = tablero.map { it.clone() }.toTypedArray()
        var movimientoRealizado = false

        when (direccion) {
            Direccion.ARRIBA -> movimientoRealizado = moverArriba()
            Direccion.ABAJO -> movimientoRealizado = moverAbajo()
            Direccion.IZQUIERDA -> movimientoRealizado = moverIzquierda()
            Direccion.DERECHA -> movimientoRealizado = moverDerecha()
        }

        if (movimientoRealizado) {
            movimientos++
            agregarNumeroAleatorio()
            if (!hayMovimientosPosibles()) {
                juegoActivo = false
            }
        }

        return movimientoRealizado
    }

    private fun moverArriba(): Boolean {
        var movimientoRealizado = false
        for (col in 0 until tamano) {
            for (fila in 1 until tamano) {
                if (tablero[fila][col] != 0) {
                    var currentFila = fila
                    while (currentFila > 0 && tablero[currentFila - 1][col] == 0) {
                        tablero[currentFila - 1][col] = tablero[currentFila][col]
                        tablero[currentFila][col] = 0
                        currentFila--
                        movimientoRealizado = true
                    }
                    if (currentFila > 0 && tablero[currentFila - 1][col] == tablero[currentFila][col]) {
                        tablero[currentFila - 1][col] *= 2
                        tablero[currentFila][col] = 0
                        movimientoRealizado = true
                    }
                }
            }
        }
        return movimientoRealizado
    }

    private fun moverAbajo(): Boolean {
        var movimientoRealizado = false
        for (col in 0 until tamano) {
            for (fila in tamano - 2 downTo 0) {
                if (tablero[fila][col] != 0) {
                    var currentFila = fila
                    while (currentFila < tamano - 1 && tablero[currentFila + 1][col] == 0) {
                        tablero[currentFila + 1][col] = tablero[currentFila][col]
                        tablero[currentFila][col] = 0
                        currentFila++
                        movimientoRealizado = true
                    }
                    if (currentFila < tamano - 1 && tablero[currentFila + 1][col] == tablero[currentFila][col]) {
                        tablero[currentFila + 1][col] *= 2
                        tablero[currentFila][col] = 0
                        movimientoRealizado = true
                    }
                }
            }
        }
        return movimientoRealizado
    }

    private fun moverIzquierda(): Boolean {
        var movimientoRealizado = false
        for (fila in 0 until tamano) {
            for (col in 1 until tamano) {
                if (tablero[fila][col] != 0) {
                    var currentCol = col
                    while (currentCol > 0 && tablero[fila][currentCol - 1] == 0) {
                        tablero[fila][currentCol - 1] = tablero[fila][currentCol]
                        tablero[fila][currentCol] = 0
                        currentCol--
                        movimientoRealizado = true
                    }
                    if (currentCol > 0 && tablero[fila][currentCol - 1] == tablero[fila][currentCol]) {
                        tablero[fila][currentCol - 1] *= 2
                        tablero[fila][currentCol] = 0
                        movimientoRealizado = true
                    }
                }
            }
        }
        return movimientoRealizado
    }

    private fun moverDerecha(): Boolean {
        var movimientoRealizado = false
        for (fila in 0 until tamano) {
            for (col in tamano - 2 downTo 0) {
                if (tablero[fila][col] != 0) {
                    var currentCol = col
                    while (currentCol < tamano - 1 && tablero[fila][currentCol + 1] == 0) {
                        tablero[fila][currentCol + 1] = tablero[fila][currentCol]
                        tablero[fila][currentCol] = 0
                        currentCol++
                        movimientoRealizado = true
                    }
                    if (currentCol < tamano - 1 && tablero[fila][currentCol + 1] == tablero[fila][currentCol]) {
                        tablero[fila][currentCol + 1] *= 2
                        tablero[fila][currentCol] = 0
                        movimientoRealizado = true
                    }
                }
            }
        }
        return movimientoRealizado
    }

    private fun agregarNumeroAleatorio() {
        val celdasVacias = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until tamano) {
            for (j in 0 until tamano) {
                if (tablero[i][j] == 0) {
                    celdasVacias.add(Pair(i, j))
                }
            }
        }

        if (celdasVacias.isNotEmpty()) {
            val (fila, col) = celdasVacias.random()
            tablero[fila][col] = if (Math.random() < 0.9) 2 else 4
        }
    }

    private fun hayMovimientosPosibles(): Boolean {
        // Verificar si hay celdas vacías
        for (i in 0 until tamano) {
            for (j in 0 until tamano) {
                if (tablero[i][j] == 0) return true
            }
        }

        // Verificar si hay movimientos posibles
        for (i in 0 until tamano) {
            for (j in 0 until tamano) {
                val valor = tablero[i][j]
                if ((i < tamano - 1 && tablero[i + 1][j] == valor) ||
                    (j < tamano - 1 && tablero[i][j + 1] == valor)) {
                    return true
                }
            }
        }

        return false
    }

    fun guardarEstado(): String {
        return tablero.joinToString(";") { fila -> fila.joinToString(",") } + "|$movimientos"
    }

    fun cargarEstado(estado: String) {
        val partes = estado.split("|")
        if (partes.size == 2) {
            val filas = partes[0].split(";")
            for (i in filas.indices) {
                val valores = filas[i].split(",").map { it.toInt() }
                for (j in valores.indices) {
                    tablero[i][j] = valores[j]
                }
            }
            movimientos = partes[1].toInt()
        }
    }
}

enum class Direccion {
    ARRIBA, ABAJO, IZQUIERDA, DERECHA
}