package com.example.movicard

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.movicard.databinding.ActivityTarjetaUuidBinding
import com.example.movicard.helper.SessionManager
import com.example.movicard.model.viewmodel.ClienteViewModel
import com.example.movicard.model.viewmodel.TarjetaViewModel
import com.example.movicard.model.viewmodel.UsuarioViewModelFactory
import com.example.movicard.network.RetrofitInstanceAPI
import com.google.android.material.navigation.NavigationView

class TarjetaUUID : BaseActivity() {
    private lateinit var binding: ActivityTarjetaUuidBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTarjetaUuidBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar la Toolbar primero
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Muestra el ícono de la hamburguesa

        // Quitar el título por defecto de la ActionBar
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Configuración de DrawerLayout después de la Toolbar
        drawerLayout = binding.drawerLayoutUUID
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


        // Manejar el clic en los botones de la parte inferior
        binding.bottomNavigationView.selectedItemId = R.id.tarjeta
        binding.bottomNavigationView.setOnItemSelectedListener(bottomNavListener)


        binding.btnLogout.setOnClickListener { logout() }

        /*
         * COMPRUEBO Y ACTIVO LA TARJETA SI LA UUID ES CORRECTA
         */

        val sessionManager = SessionManager(this)

        // creo el ViewModel usando el Factory personalizado
        val viewModelFactory = UsuarioViewModelFactory(RetrofitInstanceAPI.api, sessionManager)
        val viewModelTarjeta = ViewModelProvider(this, viewModelFactory).get(TarjetaViewModel::class.java)
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
        var tarjetaUUID: String? = null

        // extraigo la UUID de la tarjeta del cliente y la asigno al atributo tarjetaUUID
        viewModelTarjeta.tarjeta.observe(this) { tarjeta ->
            tarjetaUUID = tarjeta?.UUID
        }
        viewModelTarjeta.cargarTarjeta()

        binding.btnEntrar.setOnClickListener {
            if (binding.editTextUuid.text.toString() == tarjetaUUID) {
                showToast(getString(R.string.tarjeta_activada))
                viewModelTarjeta.cambiarEstadoTarjeta("ACTIVADA")
                viewModelTarjeta.cambiarEstadoActivacionTarjeta("ACTIVADA")
                viewModelTarjeta.cargarTarjeta()
                startActivity(Intent(this, CardSettings::class.java))
            } else {
                showToast(getString(R.string.uuid_incorrecto))
            }
        }
        // Ajustar el Navigation Drawer al 55% del ancho de la pantalla
        setDrawerWidth(binding.navView, 0.55)
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

    private fun isTargetActivated() : Boolean{
        var estaActivada : Boolean = false
        val sessionManager = SessionManager(this)
        // creo el ViewModel usando el Factory personalizado
        val viewModelFactory = UsuarioViewModelFactory(RetrofitInstanceAPI.api, sessionManager)
        val viewModelTarjeta = ViewModelProvider(this, viewModelFactory).get(TarjetaViewModel::class.java)

        viewModelTarjeta.cargarTarjeta()

        viewModelTarjeta.tarjeta.observe(this) { tarjeta ->
            estaActivada = tarjeta?.estadoactivaciontarjeta != "DESACTIVADA"
        }
        return estaActivada
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
            R.id.nav_profile -> {
                // Abrir perfil de usuario
                startActivity(Intent(this, PerfilUsuario::class.java))
            }

            R.id.nav_suscription -> {
                // Abrir suscripciones (Princin cards) si la tarjeta está activada, si no a tarjetaUUID
                if (isTargetActivated()) {
                    startActivity(Intent(this, PricingCards::class.java))
                }else {
                    mostrarDialogoTarjetaDesactivada()
                }
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
        }
        return false
    }
}