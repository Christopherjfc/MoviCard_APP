package com.example.movicard.helper

import android.content.Context
import com.example.movicard.model.Cliente

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    // se guarda el usuario de quien inició sesión
    // para así poder usar su ID en un futuro para las peticiones a la API
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
            putString("numero_bloque", cliente.numero_bloque)
            cliente.numero_piso?.let { putString("numero_piso", it) }
            putString("codigopostal", cliente.codigopostal)
            putString("password", cliente.password)
            apply()
        }
    }

    // obtengo la ID del usaurio para futuras peticiones a la API
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
            numero_bloque = prefs.getString("numero_bloque", "").toString(),
            numero_piso = if (prefs.contains("numero_piso")) prefs.getString("numero_piso", "") else null,
            codigopostal = prefs.getString("codigopostal", "") ?: "",
            password = prefs.getString("password", "") ?: ""
        )
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
