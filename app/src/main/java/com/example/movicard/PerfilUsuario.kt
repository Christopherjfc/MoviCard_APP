package com.example.movicard

import android.content.Intent
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.DisplayMetrics
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.movicard.databinding.ActivityPerfilUsuarioBinding
import com.example.movicard.databinding.ActivityPrincipalBinding
import com.google.android.material.navigation.NavigationView

class PerfilUsuario : BaseActivity() {
    private lateinit var binding: ActivityPerfilUsuarioBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        binding = ActivityPerfilUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar la Toolbar primero
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Muestra el ícono de la hamburguesa
        // Quitar el título por defecto de la ActionBar
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Configuración de DrawerLayout después de la Toolbar
        drawerLayout = binding.drawerLayoutPerfil
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

        //Desplegar la función para cambiar de contraseña
        binding.desplegarContra.setOnClickListener{
            toggleVisibility()
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }

        // Ajustar el Navigation Drawer al 55% del ancho de la pantalla
        setDrawerWidth(binding.navView, 0.55)
    }

    private fun toggleVisibility() {
        if (binding.cardCambiarContra.visibility == View.GONE) {
            TransitionManager.beginDelayedTransition(binding.cardCambiarContra, AutoTransition())
            binding.cardCambiarContra.visibility = View.VISIBLE
            binding.cardSuscripcion.visibility = View.GONE
            binding.cardUUIDTarjeta.visibility = View.GONE
            binding.desplegarContra.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.flecha_abajo, 0)
        } else {
            TransitionManager.beginDelayedTransition(binding.cardCambiarContra, AutoTransition())
            binding.cardCambiarContra.visibility = View.GONE
            binding.cardSuscripcion.visibility = View.VISIBLE
            binding.cardUUIDTarjeta.visibility = View.VISIBLE
            binding.desplegarContra.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.flecha_derecha, 0)
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


    // Listener para los elementos del menú lateral
    private val navMenuListener = NavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_suscription -> {
                // Abrir suscripciones (Princin cards)
                startActivity(Intent(this, PricingCards::class.java))
            }
            R.id.nav_config -> {
                // Abrir configuración
                startActivity(Intent(this, Settings::class.java))
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