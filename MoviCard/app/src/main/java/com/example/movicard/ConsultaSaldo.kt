package com.example.movicard

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.movicard.databinding.ActivityConsultaSaldoBinding
import com.example.movicard.helper.SessionManager
import com.example.movicard.model.viewmodel.TarjetaViewModel
import com.example.movicard.model.viewmodel.TicketViewModel
import com.example.movicard.model.viewmodel.UsuarioViewModelFactory
import com.example.movicard.network.RetrofitInstanceAPI
import com.google.android.material.navigation.NavigationView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConsultaSaldo : BaseActivity() {
    private lateinit var binding: ActivityConsultaSaldoBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private val pollingInterval = 3000L // 3 segundos
    private var pollingHandler: Handler? = null
    private var isPolling = false
    private var ultimoTipoTicket: String? = null

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

        binding.bottomNavigationView.menu.setGroupCheckable(0, true, false)
        for (i in 0 until binding.bottomNavigationView.menu.size()) {
            binding.bottomNavigationView.menu.getItem(i).isChecked = false
        }

        binding.bottomNavigationView.menu.setGroupCheckable(0, true, true)

        // Manejar el clic en los botones de la parte inferior
        binding.bottomNavigationView.setOnItemSelectedListener(bottomNavListener)

        // Cambia a la actividad donde se encuentran los tipos de tickets
        binding.btnTipoTarjeta.setOnClickListener {
            val intent = Intent(this, TicketTypes::class.java)
            startActivity(intent)
        }


        // Ajustar el Navigation Drawer al 55% del ancho de la pantalla
        setDrawerWidth(binding.navView, 0.55)


        /*
        *
        * RELLENO CAMPOS DEL USUARO CON LA API
        *
        */

        // creo el SessionManager para poder acceder a los datos guardados del usuario
        val sessionManager = SessionManager(this)

        // creo el ViewModel usando el Factory personalizado
        val viewModelFactory = UsuarioViewModelFactory(RetrofitInstanceAPI.api, sessionManager)
        val viewModelTicket = ViewModelProvider(this, viewModelFactory).get(TicketViewModel::class.java)
        val viewModelTarjeta = ViewModelProvider(this, viewModelFactory).get(TarjetaViewModel::class.java)

        viewModelTarjeta.cargarTarjeta()
        viewModelTarjeta.tarjeta.observe(this) { tarjeta ->
            if (tarjeta?.estadotarjeta == "BLOQUEADA") {
                mostrarDialogoTarjetaBloqueada()
            } else {
                inicializaUITicket(viewModelTicket)
            }
        }

        // cerrar sesión
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun mostrarDialogoTarjetaBloqueada() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Tarjeta Bloqueada")
            .setMessage("🔒 Tu tarjeta está bloqueada. No puedes consultar el saldo.\n\nVuelve a activarla desde la pantalla de configuración de tarjeta.")
            .setCancelable(false)
            .setPositiveButton("Configuración") { _, _ ->
                startActivity(Intent(this, CardSettings::class.java))
                finish()
            }
            .show()

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(
            resources.getColor(R.color.text_primary, theme)
        )
    }

    private fun empezarObservacionCantidadTENMOVI(viewModelTicket: TicketViewModel) {
        if (isPolling) return // ya está corriendo

        pollingHandler = Handler(mainLooper)
        isPolling = true

        val runnable = object : Runnable {
            override fun run() {
                viewModelTicket.cargarTicket()
                if (isPolling) {
                    pollingHandler?.postDelayed(this, pollingInterval)
                }
            }
        }

        pollingHandler?.post(runnable)
    }


    private fun detenerObservacionCantidadTENMOVI() {
        isPolling = false
        pollingHandler?.removeCallbacksAndMessages(null)
    }

    // Me formatea la fecha Date a yyyy--mm-dd
    fun formatFecha(fecha: Date?): String {
        // Formato de fecha: "yyyy-MM-dd"
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.format(fecha)
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

    private fun inicializaUITicket(viewModelTicket: TicketViewModel) {
        // Cargar y observar el ticket
        viewModelTicket.cargarTicket()

        // verifica si exite el ticket
        viewModelTicket.existeTicket { existe ->
            if (existe) {
                // si existe lo observa
                viewModelTicket.ticket.observe(this) { ticket ->
                    // si no es null entra
                    if (ticket != null) {
                        // aplico los atributos del ticket a unos nuevos para mejor visualización
                        val tipo = ticket.tipo
                        val cantidad = ticket.cantidad
                        val fechaCompra = formatFecha(ticket.fecha_inicio)
                        val diasRestantes = ticket.duracion_dias

                        when (tipo) {
                            "TENMOVI" -> {
                                binding.cantidadSaldo.text = cantidad?.toString() ?: "-"
                                binding.fechaCompra.text = fechaCompra ?: "-"
                                binding.diasRestantes.text = "∞"
                                val progreso = (cantidad?.coerceAtMost(10)?.times(10)) ?: 0
                                binding.progressBar.progress = progreso
                                ultimoTipoTicket = tipo

                                // Cada 3s cargo el ticket para ver si hay actualizaciones en la cantidad
                                if (!isPolling) {
                                    // Solo empieza el polling si no ha empezado ya
                                    empezarObservacionCantidadTENMOVI(viewModelTicket)
                                }

                            }

                            "MOVIMES", "TRIMOVI" -> {
                                binding.cantidadSaldo.text = "∞"
                                binding.fechaCompra.text = fechaCompra ?: "-"
                                binding.diasRestantes.text = diasRestantes?.toString() ?: "-"
                                binding.progressBar.progress = 100
                            }

                            else -> {
                                binding.cantidadSaldo.text = "-"
                                binding.fechaCompra.text = "-"
                                binding.diasRestantes.text = "-"
                                binding.progressBar.progress = 0
                            }
                        }
                    }
                }

            } else {
                binding.cantidadSaldo.text = "-"
                binding.fechaCompra.text = "-"
                binding.diasRestantes.text = "-"
                binding.progressBar.progress = 0
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Solo reanuda el polling si el tipo sigue siendo TENMOVI
        if (ultimoTipoTicket == "TENMOVI") {
            val sessionManager = SessionManager(this)
            val viewModelFactory = UsuarioViewModelFactory(RetrofitInstanceAPI.api, sessionManager)
            val viewModelTicket = ViewModelProvider(this, viewModelFactory).get(TicketViewModel::class.java)
            empezarObservacionCantidadTENMOVI(viewModelTicket)
        }
    }

    override fun onPause() {
        super.onPause()
        detenerObservacionCantidadTENMOVI()
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
                // Cambia a la activity CardSettings ya que la tarjeta está activada
                startActivity(Intent(this, CardSettings::class.java))
                return true
            }
        }
        return false
    }
}