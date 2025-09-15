package com.example.proyecto_final.Juegos

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.example.proyecto_final.R
import java.util.Random
import kotlin.math.pow
import kotlin.math.sqrt

class Snake : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snake)

        val board = findViewById<RelativeLayout>(R.id.board)
        val border = findViewById<RelativeLayout>(R.id.relativeLayout)
        val lilu = findViewById<LinearLayout>(R.id.lilu)
        val upButton = findViewById<Button>(R.id.up)
        val downButton = findViewById<Button>(R.id.down)
        val leftButton = findViewById<Button>(R.id.left)
        val rightButton = findViewById<Button>(R.id.right)
        val pauseButton = findViewById<Button>(R.id.pause)
        val newgame = findViewById<Button>(R.id.new_game)
        val resume = findViewById<Button>(R.id.resume)
        val playagain = findViewById<Button>(R.id.playagain)
        val score = findViewById<Button>(R.id.score)
        val score2 = findViewById<Button>(R.id.score2)
        val meat = ImageView(this)
        val snake = ImageView(this)
        val snakeSegments =
            mutableListOf(snake) // Mantiene el sentido del tablero
        val handler = Handler(Looper.getMainLooper())
        var delayMillis = 30L // Actualiza la posición de la serpiente cada 100 mili segundos
        var currentDirection = "right" // Empieza por la derecha de manera default
        var scorex = 0



        board.visibility = View.INVISIBLE
        playagain.visibility = View.INVISIBLE
        score.visibility = View.INVISIBLE
        score2.visibility = View.INVISIBLE

        newgame.setOnClickListener {


            board.visibility = View.VISIBLE
            newgame.visibility = View.INVISIBLE
            resume.visibility = View.INVISIBLE
            score2.visibility = View.VISIBLE


            snake.setImageResource(R.drawable.snake)
            snake.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            board.addView(snake)
            snakeSegments.add(snake) // Añade un segmento nuevo de la serpiente


            var snakeX = snake.x
            var snakeY = snake.y


            meat.setImageResource(R.drawable.meat)
            meat.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            board.addView(meat)

            val random = Random() // Crea un objeto Random
            val randomX =
                random.nextInt(801) - 400 // generar una coordenada x aleatoria entre -400 y 400
            val randomY =
                random.nextInt(801) - 400 // generar una coordenada y aleatoria entre -400 y 400


            meat.x = randomX.toFloat()
            meat.y = randomY.toFloat()



            fun checkFoodCollision() {
                val distanceThreshold = 50

                val distance = sqrt((snake.x - meat.x).pow(2) + (snake.y - meat.y).pow(2))

                if (distance < distanceThreshold) { // Comprueba si la distancia entre la cabeza de la serpiente y la carne es menor que el umbral

                    val newSnake =
                        ImageView(this) // Crea una nueva ImageView para el segmento de serpiente adicional
                    newSnake.setImageResource(R.drawable.snake)
                    newSnake.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    board.addView(newSnake)

                    snakeSegments.add(newSnake) // Añade el nuevo segmento de serpiente a la lista

                    val randomX =
                        random.nextInt(801) - -100
                    val randomY =
                        random.nextInt(801) - -100


                    meat.x = randomX.toFloat()
                    meat.y = randomY.toFloat()


                    delayMillis-- // Reducir el valor de retardo en 1
                    scorex++

                    score2.text =   "puntuacion : " + scorex.toString() // Actualiza la vista de texto retrasado



                }
            }




            val runnable = object : Runnable {
                override fun run() {

                    for (i in snakeSegments.size - 1 downTo 1) { // Actualiza la posición de cada segmento de la serpiente.
                        snakeSegments[i].x = snakeSegments[i - 1].x
                        snakeSegments[i].y = snakeSegments[i - 1].y
                    }


                    when (currentDirection) {
                        "arriba" -> {
                            snakeY -= 10
                            if (snakeY < -490) { // Comprueba si ImageView se sale de la parte superior del tablero
                                snakeY = -490f
                                border.setBackgroundColor(getResources().getColor(R.color.red))
                                playagain.visibility = View.VISIBLE
                                currentDirection = "pausa"
                                lilu.visibility = View.INVISIBLE

                                score.text =   "Tu puntuacion es  " + scorex.toString() // Actualizar vista de texto de retraso
                                score.visibility = View.VISIBLE
                                score2.visibility = View.INVISIBLE



                            }

                            snake.translationY = snakeY
                        }
                        "abajo" -> {
                            snakeY += 10
                            val maxY =
                                board.height / 2 - snake.height + 30 // Calcular la coordenada y máxima
                            if (snakeY > maxY) { // Comprueba si ImageView se sale de la parte inferior del tablero
                                snakeY = maxY.toFloat()
                                border.setBackgroundColor(getResources().getColor(R.color.red))
                                playagain.visibility = View.VISIBLE
                                currentDirection = "pausa"
                                lilu.visibility = View.INVISIBLE

                                score.text =   "Tu puntuacion es  " + scorex.toString() // Actualizar vista de texto de retraso
                                score.visibility = View.VISIBLE
                                score2.visibility = View.INVISIBLE


                            }
                            snake.translationY = snakeY
                        }
                        "izquierda" -> {
                            snakeX -= 10
                            if (snakeX < -490) { // Comprueba si ImageView se sale de la parte superior del tablero
                                snakeX = -490f
                                border.setBackgroundColor(getResources().getColor(R.color.red))
                                playagain.visibility = View.VISIBLE
                                currentDirection = "pausa"
                                lilu.visibility = View.INVISIBLE
                                score.text =   "Tu puntuacion es  " + scorex.toString() // Actualizar vista de texto de retraso
                                score.visibility = View.VISIBLE
                                score2.visibility = View.INVISIBLE



                            }
                            snake.translationX = snakeX
                        }
                        "derecha" -> {
                            snakeX += 10
                            val maxX =
                                board.height / 2 - snake.height + 30 // Calcular la coordenada y máxima
                            if (snakeX > maxX) { // Comprueba si ImageView se sale de la parte inferior del tablero
                                snakeX = maxX.toFloat()
                                border.setBackgroundColor(getResources().getColor(R.color.red))
                                playagain.visibility = View.VISIBLE
                                currentDirection = "pausar"
                                lilu.visibility = View.INVISIBLE

                                score.text =   "Tu puntuacion es  " + scorex.toString() // Actualizar vista de texto de retraso
                                score.visibility = View.VISIBLE
                                score2.visibility = View.INVISIBLE


                            }
                            snake.translationX = snakeX
                        }

                        "pausa" -> {
                            snakeX += 0
                            snake.translationX = snakeX
                        }
                    }

                    checkFoodCollision()
                    handler.postDelayed(this, delayMillis)
                }
            }

            handler.postDelayed(runnable, delayMillis)

// Establezca el botón onClickListeners para actualizar la variable currentDirection cuando se presione
            upButton.setOnClickListener {
                currentDirection = "ARRIBA"
            }
            downButton.setOnClickListener {
                currentDirection = "ABAJO"
            }
            leftButton.setOnClickListener {
                currentDirection = "IZQUIERDA"
            }
            rightButton.setOnClickListener {
                currentDirection = "DERECHA"
            }
            pauseButton.setOnClickListener {
                currentDirection = "PAUSA"
                board.visibility = View.INVISIBLE
                newgame.visibility = View.VISIBLE
                resume.visibility = View.VISIBLE

            }
            resume.setOnClickListener {
                currentDirection = "DERECHA"
                board.visibility = View.VISIBLE
                newgame.visibility = View.INVISIBLE
                resume.visibility = View.INVISIBLE

            }
            playagain.setOnClickListener {

                recreate()
            }

        }


    }

}
