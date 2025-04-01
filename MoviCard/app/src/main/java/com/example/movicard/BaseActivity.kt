package com.example.movicard

import android.content.SharedPreferences
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// Clase base que maneja la lógica común
open class BaseActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    var textSize: Float = 16f  // Valor por defecto

    override fun onCreate(savedInstanceState: Bundle?) {

        sharedPreferences = getSharedPreferences("config", MODE_PRIVATE)
        textSize = sharedPreferences.getFloat("textSize", 16f)  // Valor por defecto 16f

        val nightMode = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_NO)

        // Aplicar el modo oscuro antes de inflar la UI
        AppCompatDelegate.setDefaultNightMode(nightMode)

        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()

        // Cada vez que la actividad se vuelva visible, aplicar el tamaño de texto
        applyTextSize()
    }

    fun applyTextSize() {
        val textSize = sharedPreferences.getFloat("textSize", 1.0f) // Factor de escala guardado (ej: 1.2 para +20%)
        adjustTextSizeRecursively(findViewById(android.R.id.content), textSize)
    }


    private fun adjustTextSizeRecursively(view: View, scaleFactor: Float) {
        if (view is TextView) {
            val originalSize = view.tag as? Float ?: view.textSize / resources.displayMetrics.scaledDensity
            view.tag = originalSize // Guardar el tamaño original si no se ha guardado
            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, originalSize * scaleFactor)
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                adjustTextSizeRecursively(view.getChildAt(i), scaleFactor)
            }
        }
    }
}
