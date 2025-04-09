package com.example.movicard.helper

import android.content.Context
import com.example.movicard.model.Cliente

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun saveCliente(cliente: Cliente) {
        prefs.edit().apply {
            putInt("id", cliente.id)
            putString("nombre", cliente.nombre)
            putString("apellido", cliente.apellido)
            putString("correo", cliente.correo)
            putString("telefono", cliente.telefono)
            putString("direccion", cliente.direccion)
            putString("ciudad", cliente.ciudad)
            putString("dni", cliente.dni)
            putInt("numero_bloque", cliente.numero_bloque)
            cliente.numero_piso?.let { putInt("numero_piso", it) }
            putInt("codigopostal", cliente.codigopostal)
            apply()
        }
    }

    fun getCliente(): Cliente? {
        val id = prefs.getInt("id", -1)
        if (id == -1) return null

        return Cliente(
            id = id,
            nombre = prefs.getString("nombre", "") ?: "",
            apellido = prefs.getString("apellido", "") ?: "",
            correo = prefs.getString("correo", "") ?: "",
            telefono = prefs.getString("telefono", "") ?: "",
            direccion = prefs.getString("direccion", "") ?: "",
            ciudad = prefs.getString("ciudad", "") ?: "",
            dni = prefs.getString("dni", "") ?: "",
            numero_bloque = prefs.getInt("numero_bloque", 0),
            numero_piso = if (prefs.contains("numero_piso")) prefs.getInt("numero_piso", 0) else null,
            codigopostal = prefs.getInt("codigopostal", 0),
            password = "" // No lo necesitamos, se deja vacío
        )
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
