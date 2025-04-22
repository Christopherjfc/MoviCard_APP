package com.example.movicard

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.movicard.databinding.ActivityPrincipalBinding
import com.example.movicard.helper.SessionManager
import com.example.movicard.model.viewmodel.SuscripcionViewModel
import com.example.movicard.model.viewmodel.UsuarioViewModelFactory
import com.example.movicard.network.RetrofitInstance
import com.google.android.material.navigation.NavigationView

class Principal : BaseActivity() {
    private lateinit var binding: ActivityPrincipalBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Pone la bottom navegation real del celular del color que se le ponga,
        // en mi caso, el mismo color del bottom navegation de mi app

        window.navigationBarColor = ContextCompat.getColor(this, R.color.azul_main)


        // Configuración de la vista
        binding = ActivityPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Configurar la Toolbar primero
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Muestra el ícono de la hamburguesa
        // Quitar el título por defecto de la ActionBar
        supportActionBar?.setDisplayShowTitleEnabled(false)


        // Configuración de DrawerLayout después de la Toolbar
        drawerLayout = binding.drawerLayoutMain
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            binding.toolbar,
            R.string.open_nav,
            R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()


        // Aplica el color blanco a la hamburguesa del toolbar
        val iconColor = resources.getColor(R.color.white, theme)
        toggle.drawerArrowDrawable.color = iconColor


        // Manejar la navegación en el menú lateral
        binding.navView.setNavigationItemSelectedListener(navMenuListener)


        // Manejar el clic en los botones de la parte inferior
        binding.bottomNavigationView.selectedItemId = R.id.home
        binding.bottomNavigationView.setOnItemSelectedListener(bottomNavListener)


        /*
         * LE CREO UNA SUSCRIPCIÓN AL USUARIO
         */
        val sessionManager = SessionManager(this)

        // creo el ViewModel usando el Factory personalizado
        val viewModelFactory = UsuarioViewModelFactory(RetrofitInstance.api, sessionManager)
        val viewModelSuscripcion =
            ViewModelProvider(this, viewModelFactory).get(SuscripcionViewModel::class.java)

        viewModelSuscripcion.creaSuscripcion()
        viewModelSuscripcion.cargarSuscripcion()


        // Ajustar el Navigation Drawer al 55% del ancho de la pantalla
        setDrawerWidth(binding.navView, 0.55)

        binding.btnSaldo.setOnClickListener {
            startActivity(Intent(this, ConsultaSaldo::class.java))
        }

        viewModelSuscripcion.suscripcion.observe(this) { suscripcion ->
            binding.btnGraficas.setOnClickListener {
                if (suscripcion.suscripcion == "PREMIUM") {
                    startActivity(Intent(this, Graficas::class.java))
                } else {
                    mostrarDialogoPremium(getString(R.string.gr_ficas))
                }
            }

            binding.btnFacturas.setOnClickListener {
                if (suscripcion.suscripcion == "PREMIUM") {
                    startActivity(Intent(this, Invoices::class.java))
                } else {
                    mostrarDialogoPremium(getString(R.string.facturas))
                }
            }
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    /*
     * Muestro un dialogo emergente si el usuario con plan GRATUITO quieroe entrar a las
     * pantallas Graficas e Invoices. Para estas pantallas se necesita el plan PREMIUM
     *
     * Añado también un botón que redirije al usuario a la activity donde puede
     * conseguir el plan PREMIUM
     */

    private fun mostrarDialogoPremium(destino: String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.acceso_restringido))
            .setMessage(getString(R.string.cuadro_dialogo_restriccion_entrada, destino))
            .setPositiveButton(getString(R.string.ver_planes)) { _, _ ->
                startActivity(Intent(this, PricingCards::class.java))
            }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
    }


    // Méthod para ajustar el ancho del Navigation Drawer basado en un porcentaje de la pantalla
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
            R.id.nav_profile -> {
                // Abrir perfil de usuario
                startActivity(Intent(this, PerfilUsuario::class.java))
            }

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

            R.id.tarjeta -> {
                // Cambia a Tarjeta
                startActivity(Intent(this, TarjetaUUID::class.java))
                return true
            }
        }
        return false
    }
}