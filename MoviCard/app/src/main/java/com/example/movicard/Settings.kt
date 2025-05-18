package com.example.movicard

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.movicard.databinding.ActivitySettingsBinding
import com.example.movicard.helper.SessionManager
import com.example.movicard.model.viewmodel.ClienteViewModel
import com.example.movicard.model.viewmodel.TarjetaViewModel
import com.example.movicard.model.viewmodel.UsuarioViewModelFactory
import com.example.movicard.network.RetrofitInstanceAPI
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

        val sessionManager = SessionManager(this)

        // creo el ViewModel usando el Factory personalizado
        val viewModelFactory = UsuarioViewModelFactory(RetrofitInstanceAPI.api, sessionManager)
        val viewModelcliente = ViewModelProvider(this, viewModelFactory).get(ClienteViewModel::class.java)

        // obtengo el menu drawe y busco su textView para sustituirlo
        val headerView = binding.navView.getHeaderView(0)
        val nombreMenuDrawer = headerView.findViewById<TextView>(R.id.nombre)

        // observo el LiveData del cliente
        viewModelcliente.cliente.observe(this) { cliente ->
            // actualizo el nombre del menu drawer con el usuario actual
            nombreMenuDrawer.text = cliente.nombre + " " + cliente.apellido
        }

        // actualizamos el observe con los nuevos datos
        viewModelcliente.cargarCliente()

        // Ajustar el Navigation Drawer al 55% del ancho de la pantalla
        setDrawerWidth(binding.navView, 0.55)

        val languages = listOf(getString(R.string.idioma), "ES", "EN", "CA")
        configureLanguageSpinner(binding.spinnerLanguage, languages)

        // Size options for the text size spinner
        val sizes = listOf(getString(R.string.tama_o), "+1", "+2", "-1", "-2")
        configureSizeSpinner(binding.spinnerSize, sizes)

        binding.switchTemas.isChecked

        /*
        *
        * MODO CLARO / OSCURO
        *
        */

        // Cargar SharedPreferences
        sharedPreferences = getSharedPreferences("config", MODE_PRIVATE)

        // Leer estado del tema guardado
        val isDarkMode = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_NO) == AppCompatDelegate.MODE_NIGHT_YES

        // Deshabilitar temporalmente el listener para evitar activación no deseada
        binding.switchTemas.setOnCheckedChangeListener(null)
        binding.switchTemas.isChecked = isDarkMode
        binding.switchTemas.setOnCheckedChangeListener { _, isChecked ->
            val newMode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO

            with(sharedPreferences.edit()) {
                putInt("theme_mode", newMode)
                apply()
            }

            if (AppCompatDelegate.getDefaultNightMode() != newMode) {
                AppCompatDelegate.setDefaultNightMode(newMode)
                finish()
                startActivity(intent)
            }
        }

    }

    private fun configureLanguageSpinner(spinner: Spinner, options: List<String>) {
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, options) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).setTextColor(resources.getColor(R.color.white, theme)) // Establecer color aquí
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).setTextColor(resources.getColor(R.color.text_cambiar_contra, theme)) // Establecer color aquí también
                return view
            }
        }

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
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, options) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).setTextColor(resources.getColor(R.color.white, theme)) // Establecer color aquí
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).setTextColor(resources.getColor(R.color.text_cambiar_contra, theme)) // Establecer color aquí también
                return view
            }
        }

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

    private fun mostrarDialogoTarjetaDesactivada() {
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.acceso_restringido))
            .setMessage(getString(R.string.activar_uuid_para_funciones_principales))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.activa_uuid)) { _, _ ->
                startActivity(Intent(this, TarjetaUUID::class.java))
            }
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
    }

    private fun isTargetActivated(callback: (Boolean) -> Unit) {
        val sessionManager = SessionManager(this)
        val viewModelFactory = UsuarioViewModelFactory(RetrofitInstanceAPI.api, sessionManager)
        val viewModelTarjeta = ViewModelProvider(this, viewModelFactory).get(TarjetaViewModel::class.java)

        viewModelTarjeta.cargarTarjeta()

        viewModelTarjeta.tarjeta.observe(this) { tarjeta ->
            val activada = tarjeta?.estadoactivaciontarjeta != "DESACTIVADA"
            callback(activada)
        }
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
                // Abrir suscripciones (Princin cards) si la tarjeta está activada, si no a tarjetaUUID
                isTargetActivated { activada ->
                    if (activada) {
                        startActivity(Intent(this, PricingCards::class.java))
                    } else {
                        mostrarDialogoTarjetaDesactivada()
                    }
                }
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
                // Cambia a CardSettings si la tarjeta está activada, si no a TarejetaUUID
                isTargetActivated { activada ->
                    if (activada) {
                        startActivity(Intent(this, CardSettings::class.java))
                    } else {
                        mostrarDialogoTarjetaDesactivada()
                    }
                }
                return true
            }
        }
        return false
    }
}

