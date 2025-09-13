package com.example.proyecto_final

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_final.database.DatabaseHelper
import com.example.proyecto_final.utils.SessionManager

class LoginActivity : AppCompatActivity() {

    // Vistas
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar helpers
        databaseHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        // Verificar si ya hay una sesión activa
        if (sessionManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        initViews()
        setupClickListeners()
    }

    // Inicializar vistas
    private fun initViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)
    }

    // Configurar listeners de clic
    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            performLogin()
        }
    // Navegar hasta la actividad de registro
        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun performLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (validateInput(email, password)) {
            // Verificar credenciales en la base de datos
            if (databaseHelper.loginUser(email, password)) {
                // Obtener información del usuario y crear sesión
                val userInfo = databaseHelper.getUserInfo(email)
                if (userInfo != null) {
                    sessionManager.createSession(userInfo)
                    Toast.makeText(this, "Bienvenido ${userInfo.name}", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Error al obtener información del usuario", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            etEmail.error = "El email es requerido"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Email inválido"
            return false
        }

        if (password.isEmpty()) {
            etPassword.error = "La contraseña es requerida"
            return false
        }

        if (password.length < 6) {
            etPassword.error = "La contraseña debe tener al menos 6 caracteres"
            return false
        }

        return true
    }
}
