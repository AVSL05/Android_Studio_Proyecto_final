package com.example.proyecto_final.juegos

data class Card(
    val id: Int,
    val imageResId: Int,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)

