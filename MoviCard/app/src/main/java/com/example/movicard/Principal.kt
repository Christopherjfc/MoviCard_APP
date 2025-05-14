package com.example.movicard

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.movicard.databinding.ActivityPrincipalBinding
import com.example.movicard.helper.SessionManager
import com.example.movicard.model.viewmodel.ClienteViewModel
import com.example.movicard.model.viewmodel.SuscripcionViewModel
import com.example.movicard.model.viewmodel.TarjetaViewModel
import com.example.movicard.model.viewmodel.UsuarioViewModelFactory
import com.example.movicard.network.RetrofitInstanceAPI
import com.google.android.material.navigation.NavigationView

class Principal : BaseActivity() {
    private lateinit var binding: ActivityPrincipalBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private var tarjetaActivada: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configuración de la vista
        binding = ActivityPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Pone la bottom navegation real del celular del color que se le ponga,
        // en mi caso, el mismo color del bottom navegation de mi app
        window.navigationBarColor = ContextCompat.getColor(this, R.color.azul_main)

        // Configura primero el Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Muestra el ícono de la hamburguesa
        // Quita el título por defecto del ActionBar
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

        // Ajustar el Navigation Drawer al 55% del ancho de la pantalla
        setDrawerWidth(binding.navView, 0.55)

        binding.btnSaldo.setOnClickListener {
            startActivity(Intent(this, ConsultaSaldo::class.java))
        }

        /*
         * LE CREO UNA TARJETA MOVICARD AL USUARIO
         */

        val sessionManager = SessionManager(this)

        // creo el ViewModel usando el Factory personalizado
        val viewModelFactory = UsuarioViewModelFactory(RetrofitInstanceAPI.api, sessionManager)
        val viewModelSuscripcion = ViewModelProvider(this, viewModelFactory).get(SuscripcionViewModel::class.java)
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

        viewModelSuscripcion.cargarSuscripcion()

        // Creo Tarjeta MoviCard y gestiono actividades con AlertDialog si
        // el usuario no tiene un plan PREMIUM para poder acceder a ellas
        viewModelSuscripcion.suscripcion.observe(this) { suscripcion ->
            viewModelTarjeta.crearTarjeta(suscripcion.id)
            viewModelTarjeta.cargarTarjeta()

            viewModelTarjeta.tarjeta.observe(this) { tarjeta ->
                tarjetaActivada = tarjeta?.estadoactivaciontarjeta == "ACTIVADA"

                // Asignar listeners SOLO cuando ya tienes el estado de la tarjeta
                binding.btnSaldo.setOnClickListener {
                    if (tarjetaActivada) {
                        startActivity(Intent(this, ConsultaSaldo::class.java))
                    } else {
                        mostrarDialogoTarjetaDesactivada()
                    }
                }

                binding.btnGraficas.setOnClickListener {
                    if (tarjetaActivada) {
                        if (suscripcion.suscripcion == "PREMIUM") {
                            startActivity(Intent(this, Graficas::class.java))
                        } else {
                            mostrarDialogoPremium(getString(R.string.gr_ficas))
                        }
                    } else {
                        mostrarDialogoTarjetaDesactivada()
                    }
                }

                binding.btnFacturas.setOnClickListener {
                    if (tarjetaActivada) {
                        if (suscripcion.suscripcion == "PREMIUM") {
                            startActivity(Intent(this, Invoices::class.java))
                        } else {
                            mostrarDialogoPremium(getString(R.string.facturas))
                        }
                    } else {
                        mostrarDialogoTarjetaDesactivada()
                    }
                }
            }
        }
        binding.btnRuta.setOnClickListener { 
            startActivity(Intent(this, Routes::class.java))
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }
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

    /*
     * Muestro un dialogo emergente si el usuario con plan GRATUITO quieroe entrar a las
     * pantallas Graficas e Invoices. Para estas pantallas se necesita el plan PREMIUM
     *
     * Añado también un botón que redirije al usuario a la activity donde puede
     * conseguir el plan PREMIUM
     */

    private fun mostrarDialogoPremium(nombreActivity: String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.acceso_restringido))
            .setMessage(getString(R.string.cuadro_dialogo_restriccion_entrada, nombreActivity))
            .setPositiveButton(getString(R.string.ver_planes)) { _, _ ->
                startActivity(Intent(this, PricingCards::class.java))
            }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
    }

    private fun mostrarDialogoTarjetaDesactivada() {
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.acceso_restringido))
            .setMessage(getString(R.string.activar_uuid_para_funciones_principales))
            .setPositiveButton(getString(R.string.activa_uuid)) { _, _ ->
                startActivity(Intent(this, TarjetaUUID::class.java))
            }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
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

            R.id.tarjeta -> {
                // Cambia a CardSettings si la tarjeta está activada, si no a TarejetaUUID
                if (isTargetActivated()) {
                    startActivity(Intent(this, CardSettings::class.java))
                }else {
                    mostrarDialogoTarjetaDesactivada()
                }
                return true
            }
        }
        return false
    }
}