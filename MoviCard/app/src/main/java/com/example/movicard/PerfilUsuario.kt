package com.example.movicard

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.DisplayMetrics
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.movicard.databinding.ActivityPerfilUsuarioBinding
import com.example.movicard.databinding.ActivityPrincipalBinding
import com.example.movicard.helper.SessionManager
import com.example.movicard.model.viewmodel.ClienteViewModel
import com.example.movicard.model.viewmodel.ClienteViewModelFactory
import com.example.movicard.network.RetrofitInstance
import com.google.android.material.navigation.NavigationView
import java.io.File
import java.io.FileOutputStream

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
            toggleVisibilityCambiarContra()
        }

        binding.desplegarDatos.setOnClickListener {
            toggleVisibilityCambiarDatos()
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }

        // Ajustar el Navigation Drawer al 55% del ancho de la pantalla
        setDrawerWidth(binding.navView, 0.55)


        /*
        *
        * RELLENO CAMPOS DEL USUARO CON LA API Y NO MANUALMENTE
        *
        */

        // creo el SessionManager para poder acceder a los datos guardados del usuario
        val sessionManager = SessionManager(this)

        // creo el ViewModel usando el Factory personalizado
        val viewModelFactory = ClienteViewModelFactory(RetrofitInstance.api, sessionManager)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(ClienteViewModel::class.java)

        // observo el LiveData del cliente y actualizo la UI cuando llegue la respuesta
        viewModel.cliente.observe(this) { cliente ->
            // actualizo los campos de la interfaz con los datos del cliente
            binding.nombrePerfil.setText(cliente.nombre + cliente.apellido)
            binding.correo.setText(cliente.correo)
        }

        // Llamamos a la función para iniciar la carga de datos del cliente
        viewModel.cargarCliente()
    }


    private fun toggleVisibilityCambiarContra() {
        // Cambiar de contraseña
        if (binding.cardCambiarContra.visibility == View.GONE) {
            TransitionManager.beginDelayedTransition(binding.cardCambiarContra, AutoTransition())
            binding.cardCambiarContra.visibility = View.VISIBLE
            binding.cardSuscripcion.visibility = View.GONE
            binding.cardUUIDTarjeta.visibility = View.GONE
            binding.btnDatosPersonales.visibility = View.GONE
            binding.desplegarContra.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.flecha_abajo, 0)
        } else {
            TransitionManager.beginDelayedTransition(binding.cardCambiarContra, AutoTransition())
            binding.cardCambiarContra.visibility = View.GONE
            binding.cardSuscripcion.visibility = View.VISIBLE
            binding.cardUUIDTarjeta.visibility = View.VISIBLE
            binding.btnDatosPersonales.visibility = View.VISIBLE
            binding.desplegarContra.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.flecha_derecha, 0)
        }
    }

    private fun toggleVisibilityCambiarDatos() {
        // Cambiar datos personales
        if (binding.cardDatosPersonales.visibility == View.GONE) {
            TransitionManager.beginDelayedTransition(binding.cardDatosPersonales, AutoTransition())
            binding.cardDatosPersonales.visibility = View.VISIBLE
            binding.cardSuscripcion.visibility = View.GONE
            binding.cardUUIDTarjeta.visibility = View.GONE
            binding.btnCambiarContra.visibility = View.GONE
            binding.desplegarDatos.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.flecha_abajo, 0)
        } else {
            TransitionManager.beginDelayedTransition(binding.cardDatosPersonales, AutoTransition())
            binding.cardDatosPersonales.visibility = View.GONE
            binding.cardSuscripcion.visibility = View.VISIBLE
            binding.btnCambiarContra.visibility = View.VISIBLE
            binding.cardUUIDTarjeta.visibility = View.VISIBLE
            binding.desplegarDatos.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.flecha_derecha, 0)
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