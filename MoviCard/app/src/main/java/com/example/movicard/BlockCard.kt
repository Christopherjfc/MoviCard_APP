package com.example.movicard

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.movicard.databinding.ActivityBlockCardBinding
import com.example.movicard.helper.SessionManager
import com.example.movicard.model.viewmodel.ClienteViewModel
import com.example.movicard.model.viewmodel.TarjetaViewModel
import com.example.movicard.model.viewmodel.UsuarioViewModelFactory
import com.example.movicard.network.RetrofitInstanceAPI
import com.google.android.material.navigation.NavigationView

class BlockCard : BaseActivity() {

    private lateinit var binding: ActivityBlockCardBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockCardBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Configurar la Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)


        // Configurar el menú lateral
        drawerLayout = binding.drawerLayoutBockCard
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, binding.toolbar,
            R.string.open_nav, R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()



        val iconColor = resources.getColor(R.color.white, theme)
        toggle.drawerArrowDrawable.color = iconColor


        // Configurar la navegación
        binding.navView.setNavigationItemSelectedListener(navMenuListener)



        binding.bottomNavigationView.menu.setGroupCheckable(0, true, false)
        for (i in 0 until binding.bottomNavigationView.menu.size()) {
            binding.bottomNavigationView.menu.getItem(i).isChecked = false
        }
        binding.bottomNavigationView.menu.setGroupCheckable(0, true, true)
        binding.bottomNavigationView.setOnItemSelectedListener(bottomNavListener)


        // Ajustar el Navigation Drawer al 55% del ancho de la pantalla
        setDrawerWidth(binding.navView, 0.55)

        binding.btnLogout.setOnClickListener { logout() }

        // activo el botón bloquear y descactivado el botón de desbloquear
        binding.btnBloquear.isEnabled = true
        binding.btnDesbloquear.isEnabled = false

        /*
        * CAMBIO EL ESTADO DE LA TARJETA CON LA API
        */

        // creo el SessionManager para poder acceder a los datos guardados del usuario
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

        viewModelTarjeta.cargarTarjeta()

        binding.btnBloquear.setOnClickListener {
            viewModelTarjeta.cambiarEstadoTarjeta("BLOQUEADA")
            binding.btnBloquear.isEnabled = false
            binding.btnBloquear.setBackgroundColor(getColor(R.color.btn_gris))
            binding.btnDesbloquear.setBackgroundColor(getColor(R.color.azul_main))
            binding.btnDesbloquear.isEnabled = true
            viewModelTarjeta.cargarTarjeta()
        }

        binding.btnDesbloquear.setOnClickListener {
            viewModelTarjeta.cambiarEstadoTarjeta("ACTIVADA")
            binding.btnDesbloquear.isEnabled = false
            binding.btnDesbloquear.setBackgroundColor(getColor(R.color.btn_gris))
            binding.btnBloquear.setBackgroundColor(getColor(R.color.azul_main))
            binding.btnBloquear.isEnabled = true
            viewModelTarjeta.cargarTarjeta()
        }

        viewModelTarjeta.tarjeta.observe(this) { tarjeta ->
            if (tarjeta?.estadotarjeta == "ACTIVADA") {
                binding.cardStatus.text = getString(R.string.activada)
                binding.btnDesbloquear.isEnabled = false
                binding.btnDesbloquear.setBackgroundColor(getColor(R.color.btn_gris))
                binding.btnBloquear.setBackgroundColor(getColor(R.color.azul_main))
                binding.btnBloquear.isEnabled = true
            } else {
                binding.cardStatus.text = getString(R.string.bloqueada)
                binding.btnBloquear.isEnabled = false
                binding.btnBloquear.setBackgroundColor(getColor(R.color.btn_gris))
                binding.btnDesbloquear.setBackgroundColor(getColor(R.color.azul_main))
                binding.btnDesbloquear.isEnabled = true
            }
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
                startActivity(Intent(this, CardSettings::class.java))
                return true
            }
        }
        return false
    }
}