package com.example.movicard

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.movicard.databinding.ActivitySettingsBinding
import com.google.android.material.navigation.NavigationView
import java.util.Locale

class Settings : BaseActivity() { // Cambiar de AppCompatActivity a BaseActivity
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var sharedPreferences: SharedPreferences
    private var currentLanguage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("config", MODE_PRIVATE)
        currentLanguage = sharedPreferences.getString("language", "ES") // Cargar el idioma guardado

        // Set up the Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Set up the navigation menu
        drawerLayout = binding.drawerLayoutSettings
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, binding.toolbar,
            R.string.open_nav, R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val iconColor = resources.getColor(R.color.white, theme)
        toggle.drawerArrowDrawable.color = iconColor

        // Set up the navigation
        binding.navView.setNavigationItemSelectedListener(navMenuListener)

        binding.bottomNavigationView.menu.setGroupCheckable(0, true, false)
        for (i in 0 until binding.bottomNavigationView.menu.size()) {
            binding.bottomNavigationView.menu.getItem(i).isChecked = false
        }
        binding.bottomNavigationView.menu.setGroupCheckable(0, true, true)
        binding.bottomNavigationView.setOnItemSelectedListener(bottomNavListener)

        binding.btnLogout.setOnClickListener { logout() }

        val languages = listOf(getString(R.string.idioma), "ES", "EN", "CA")
        configureLanguageSpinner(binding.spinnerLanguage, languages)

        // Size options for the text size spinner
        val sizes = listOf(getString(R.string.tama_o), "+1", "+2", "-1", "-2")
        configureSizeSpinner(binding.spinnerSize, sizes)


        // Ajustar el Navigation Drawer al 55% del ancho de la pantalla
        setDrawerWidth(binding.navView, 0.55)
    }

    private fun configureLanguageSpinner(spinner: Spinner, options: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    val selected = parent.getItemAtPosition(position).toString()
                    changeLanguage(selected)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun configureSizeSpinner(spinner: Spinner, options: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = parent.getItemAtPosition(position).toString()
                val currentScale = sharedPreferences.getFloat("textSize", 1.0f)
                val newScale = when (selected) {
                    "+1" -> currentScale + 0.1f
                    "+2" -> currentScale + 0.2f
                    "-1" -> currentScale - 0.1f
                    "-2" -> currentScale - 0.2f
                    else -> 1.0f
                }

                sharedPreferences.edit().putFloat("textSize", newScale).apply()
                applyTextSize() // Aplicar el cambio en toda la app
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }



    private fun changeLanguage(language: String) {
        val locale = when (language) {
            "ES" -> Locale("es", "ES")
            "EN" -> Locale("en", "US")
            "CA" -> Locale("ca", "ES")
            else -> Locale("es", "ES")
        }

        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)

        // Restart activity to apply language change
        finish()
        startActivity(intent)
    }


    // Método para ajustar el ancho del Navigation Drawer basado en un porcentaje de la pantalla
    private fun setDrawerWidth(navView: NavigationView, percentage: Double) {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels

        // Calcular el ancho del Drawer con el porcentaje especificado
        val drawerWidth = (screenWidth * percentage).toInt()

        // Aplicar el nuevo ancho al NavigationView
        val layoutParams = navView.layoutParams
        layoutParams.width = drawerWidth
        navView.layoutParams = layoutParams
    }


    // menu lateral
    private val navMenuListener = NavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_profile -> {
                startActivity(Intent(this, PerfilUsuario::class.java))
            }
            R.id.nav_suscription -> {
                // Abrir suscripciones (Princin cards)
                startActivity(Intent(this, PricingCards::class.java))
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        true
    }

    private fun logout() {
        startActivity(Intent(this, Login::class.java))
        finish()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressedDispatcher.onBackPressed()
        }
    }

    private val bottomNavListener = fun(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.help -> {
                // Cambia a ayuda
                startActivity(Intent(this, Help::class.java))
                return true
            }
            R.id.home -> {
                // Cambia a pantalla principal
                startActivity(Intent(this, Principal::class.java))
                return true
            }
            R.id.tarjeta -> {
                // Cambia a Tarjeta
                startActivity(Intent(this, TarjetaUUID::class.java))
                return true
            }
        }
        return false
    }
}

