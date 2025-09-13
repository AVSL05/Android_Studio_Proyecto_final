package com.example.proyecto_final.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.proyecto_final.database.UserInfo

// Maneja la sesión del usuario usando SharedPreferences
class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // Claves para SharedPreferences
    companion object {
        private const val PREF_NAME = "user_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
    }

    // Crear una nueva sesión de usuario usando un objeto UserInfo
    fun createSession(userInfo: UserInfo) {
        sharedPreferences.edit {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putInt(KEY_USER_ID, userInfo.id)
            putString(KEY_USER_NAME, userInfo.name)
            putString(KEY_USER_EMAIL, userInfo.email)
        }
    }

    // Verificar si el usuario está logueado
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // Obtener información del usuario desde la sesión
    fun getUserName(): String? {
        return sharedPreferences.getString(KEY_USER_NAME, null)
    }

    // Obtener email del usuario desde la sesión
    fun getUserEmail(): String? {
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }

    // Cerrar sesión y limpiar datos
    fun logout() {
        sharedPreferences.edit {
            clear()
        }
    }
}
