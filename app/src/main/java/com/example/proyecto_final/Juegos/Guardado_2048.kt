package com.example.a2048

import android.content.Context
import android.content.SharedPreferences

class Guardado_2048(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("JUEGO_2048", Context.MODE_PRIVATE)

    fun guardarPartida(tamano: Int, estado: String) {
        prefs.edit().putString("PARTIDA_$tamano", estado).apply()
    }

    fun cargarPartida(tamano: Int): String? {
        return prefs.getString("PARTIDA_$tamano", null)
    }

    fun limpiarPartida(tamano: Int) {
        prefs.edit().remove("PARTIDA_$tamano").apply()
    }
}