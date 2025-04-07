package com.example.movicard

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Tarjeta(
    val nombre : String
)

object TarjetaStorage {

    private const val PREF_NAME = "TarjetasPrefs"
    private const val KEY_TARJETAS = "lista_tarjetas"

    fun guardarTarjetas(context: Context, tarjetas: List<Tarjeta>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val json = Gson().toJson(tarjetas)
        editor.putString(KEY_TARJETAS, json)
        editor.apply()
    }

    fun cargarTarjetas(context: Context): MutableList<Tarjeta> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_TARJETAS, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Tarjeta>>() {}.type
        return Gson().fromJson(json, type)
    }
}
