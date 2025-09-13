package com.example.proyecto_final.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest

// Esto maneja SQLite y las operaciones CRUD
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // Definiciones de la base de datos
    companion object {
        private const val DATABASE_NAME = "ProyectoFinal.db"
        private const val DATABASE_VERSION = 1

        // Tabla de usuarios
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_CREATED_AT = "created_at"
    }

    // Crear la tabla de usuarios
    override fun onCreate(db: SQLiteDatabase?) {
        val createUserTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()

        db?.execSQL(createUserTable)
    }


    // Actualizar la base de datos (si es necesario)
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // Función para hashear contraseñas Importante para seguridad
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    // Registrar un nuevo usuario
    fun registerUser(name: String, email: String, password: String): Boolean {
        val db = this.writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COLUMN_NAME, name)
                put(COLUMN_EMAIL, email.lowercase())
                put(COLUMN_PASSWORD, hashPassword(password))
            }

            val result = db.insert(TABLE_USERS, null, values)
            result != -1L
        } catch (_: Exception) {
            false
        } finally {
            db.close()
        }
    }

    // Verificar login de usuario
    fun loginUser(email: String, password: String): Boolean {
        val db = this.readableDatabase
        return try {
            val hashedPassword = hashPassword(password)
            val cursor = db.query(
                TABLE_USERS,
                arrayOf(COLUMN_ID),
                "$COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?",
                arrayOf(email.lowercase(), hashedPassword),
                null, null, null
            )

            val userExists = cursor.count > 0
            cursor.close()
            userExists
        } catch (_: Exception) {
            false
        } finally {
            db.close()
        }
    }

    // Verificar si el email ya existe
    fun emailExists(email: String): Boolean {
        val db = this.readableDatabase
        return try {
            val cursor = db.query(
                TABLE_USERS,
                arrayOf(COLUMN_ID),
                "$COLUMN_EMAIL = ?",
                arrayOf(email.lowercase()),
                null, null, null
            )

            val exists = cursor.count > 0
            cursor.close()
            exists
        } catch (_: Exception) {
            false
        } finally {
            db.close()
        }
    }

    // Obtener información del usuario por email
    fun getUserInfo(email: String): UserInfo? {
        val db = this.readableDatabase
        return try {
            val cursor = db.query(
                TABLE_USERS,
                arrayOf(COLUMN_ID, COLUMN_NAME, COLUMN_EMAIL),
                "$COLUMN_EMAIL = ?",
                arrayOf(email.lowercase()),
                null, null, null
            )

            if (cursor.moveToFirst()) {
                val userInfo = UserInfo(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL))
                )
                cursor.close()
                userInfo
            } else {
                cursor.close()
                null
            }
        } catch (_: Exception) {
            null
        } finally {
            db.close()
        }
    }
}

// Clase de datos para la información del usuario
data class UserInfo(
    val id: Int,
    val name: String,
    val email: String
)
