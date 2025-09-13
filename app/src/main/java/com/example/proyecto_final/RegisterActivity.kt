package com.example.proyecto_final

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_final.database.DatabaseHelper

// Maneja el registro de nuevos usuarios

class RegisterActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        databaseHelper = DatabaseHelper(this)

        initViews()
        setupClickListeners()
    }

    // Inicializar vistas
    private fun initViews() {
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)
    }

    // Configurar listeners de clic
    private fun setupClickListeners() {
        btnRegister.setOnClickListener {
            performRegister()
        }

        tvLogin.setOnClickListener {
            finish()
        }
    }

    // Manejar el registro
    private fun performRegister() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (validateInput(name, email, password, confirmPassword)) {
            // Verificar si el email ya existe
            if (databaseHelper.emailExists(email)) {
                etEmail.error = "Este email ya está registrado"
                Toast.makeText(this, "El email ya está registrado", Toast.LENGTH_SHORT).show()
                return
            }

            // Registrar el usuario en la base de datos
            if (databaseHelper.registerUser(name, email, password)) {
                Toast.makeText(this, "Registro exitoso. Ahora puedes iniciar sesión", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error al registrar el usuario. Inténtalo de nuevo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Validar entradas del usuario
    private fun validateInput(name: String, email: String, password: String, confirmPassword: String): Boolean {
        if (name.isEmpty()) {
            etName.error = "El nombre es requerido"
            return false
        }

        if (name.length > 35) {
            etName.error = "El nombre no puede tener más de 35 caracteres"
            return false
        }

        if (email.isEmpty()) {
            etEmail.error = "El email es requerido"
            return false
        }

        if (email.length > 35) {
            etEmail.error = "El email no puede tener más de 35 caracteres"
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

        if (password.length > 35) {
            etPassword.error = "La contraseña no puede tener más de 35 caracteres"
            return false
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.error = "Confirma tu contraseña"
            return false
        }

        if (confirmPassword.length > 35) {
            etConfirmPassword.error = "La confirmación no puede tener más de 35 caracteres"
            return false
        }

        if (password != confirmPassword) {
            etConfirmPassword.error = "Las contraseñas no coinciden"
            return false
        }

        return true
    }
}
