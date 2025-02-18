package com.example.movicard

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.movicard.databinding.ActivityPrincipalBinding
import com.example.movicard.databinding.ActivitySettingsBinding
import com.google.android.material.navigation.NavigationView
import java.util.Locale

class Settings : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Configuración de la vista
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar la Toolbar primero
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Muestra el ícono de la hamburguesa
        // Quitar el título por defecto de la ActionBar
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Configuración de DrawerLayout después de la Toolbar
        drawerLayout = binding.drawerLayoutSettings
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            binding.toolbar,
            R.string.open_nav,
            R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val iconColor = resources.getColor(R.color.white, theme)
        toggle.drawerArrowDrawable.color = iconColor

        // Manejar la navegación en el menú lateral
        binding.navView.setNavigationItemSelectedListener(navMenuListener)

        // desenmarca todos los items del menu bottomNavegation
        binding.bottomNavigationView.menu.setGroupCheckable(0, true, false)
        for (i in 0 until binding.bottomNavigationView.menu.size()) {
            binding.bottomNavigationView.menu.getItem(i).isChecked = false
        }
        binding.bottomNavigationView.menu.setGroupCheckable(0, true, true)

        // Manejar el menú de navegación inferior
        binding.bottomNavigationView.setOnItemSelectedListener(bottomNavListener)

        binding.btnLogout.setOnClickListener {
            logout()
        }

        val idiomas = listOf(getString(R.string.idioma),"ES", "EN", "CA")
        val sizes = listOf(getString(R.string.tama_o), "12", "14", "16", "18")

        configurarSpinner(binding.spinerIdioma, idiomas)
        configurarSpinner(binding.spinnerSize, sizes)
    }

    // configuración para seleccionar una opción del spinner
    private fun configurarSpinner(spinner: Spinner, opciones: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    val seleccionado = parent.getItemAtPosition(position).toString()
                    Toast.makeText(applicationContext, seleccionado, Toast.LENGTH_SHORT).show()

                    // Llamo a la función que cambiará el idioma
                    cambiarIdioma(seleccionado)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // Función para cambiar el idioma de la app
    private fun cambiarIdioma(idioma: String) {
        val locale = when (idioma) {
            "ES" -> Locale("es", "ES")
            "EN" -> Locale("en", "US")
            "CA" -> Locale("ca", "ES")
            else -> Locale("es", "ES") // Idioma por defecto
        }

        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)

        // Aplicar el nuevo contexto de configuración sin afectar el sistema
        createConfigurationContext(config)

        // Reiniciar la actividad para que los cambios surtan efecto
        recreate()
    }

    // Listener para los elementos del menú lateral
    private val navMenuListener = NavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_profile -> {
                // Abrir perfil de usuario
                startActivity(Intent(this, PerfilUsuario::class.java))
            }
        }
        // Cerrar el menú una vez que se haya seleccionado un item
        drawerLayout.closeDrawer(GravityCompat.START)
        true
    }

    // Lógica de logout
    private fun logout() {
        startActivity(Intent(this, Login::class.java))
        finish()
    }

    override fun onBackPressed() {
        // Si el drawer está abierto, cerrarlo al presionar la tecla de retroceso
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressedDispatcher
        }
    }

    private val bottomNavListener = fun(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                // Cambia a Home
                startActivity(Intent(this, Principal::class.java))
                return true
            }

            R.id.tarjeta -> {
                // Cambia a Home
                startActivity(Intent(this, TarjetaUUID::class.java))
                return true
            }
        }
        return false
    }
}
