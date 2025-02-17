package com.example.movicard

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.movicard.databinding.ActivityConsultaSaldoBinding
import com.example.movicard.databinding.ActivityPrincipalBinding
import com.google.android.material.navigation.NavigationView

class ConsultaSaldo : AppCompatActivity() {
    private lateinit var binding: ActivityConsultaSaldoBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityConsultaSaldoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar la Toolbar primero
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Muestra el ícono de la hamburguesa
        // Quitar el título por defecto de la ActionBar
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Configuración de DrawerLayout después de la Toolbar
        drawerLayout = binding.drawerLayoutConsulta
        toggle = ActionBarDrawerToggle(this, drawerLayout, binding.toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val iconColor = resources.getColor(R.color.white, theme)
        toggle.drawerArrowDrawable.color = iconColor

        // Manejar la navegación en el menú lateral
        binding.navView.setNavigationItemSelectedListener(navMenuListener)


        binding.bottomNavigationView.setOnItemSelectedListener(bottomNavListener)

        binding.btnEntrar.setOnClickListener{
            startActivity(Intent(this, RegistraTarjeta::class.java))
        }
    }

    // Listener para los elementos del menú lateral
    private val navMenuListener = NavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_profile -> {
                // Abrir perfil de usuario
                startActivity(Intent(this, PerfilUsuario::class.java))
            }
            R.id.nav_config -> {
                // Abrir configuración
                startActivity(Intent(this, Settings::class.java))
            }
            R.id.nav_logout -> {
                // Realizar logout (deberías agregar la lógica correspondiente)
                logout()
            }
        }
        // Cerrar el menú una vez que se haya seleccionado un item
        drawerLayout.closeDrawer(GravityCompat.START)
        true
    }

    // Lógica de logout
    private fun logout() {
        startActivity(Intent(this, Login ::class.java))
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

    private val bottomNavListener = fun(item: MenuItem): Boolean{
        when (item.itemId) {
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