package com.example.movicard

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.movicard.databinding.ActivityPurchaseSummaryBinding
import com.example.movicard.helper.SessionManager
import com.example.movicard.model.viewmodel.SuscripcionViewModel
import com.example.movicard.model.viewmodel.TarjetaViewModel
import com.example.movicard.model.viewmodel.TicketViewModel
import com.example.movicard.model.viewmodel.UsuarioViewModelFactory
import com.example.movicard.network.RetrofitInstance
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PurchaseSummary : BaseActivity() {
    private lateinit var binding: ActivityPurchaseSummaryBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPurchaseSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar la Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)


        // Configurar el menú lateral
        drawerLayout = binding.drawerLayoutPurchaseSummary
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


        // Desenmarco todos iconos del menu inferior porque esta clase no pertenece a
        // ni uno de esos 3
        binding.bottomNavigationView.menu.setGroupCheckable(0, true, false)
        for (i in 0 until binding.bottomNavigationView.menu.size()) {
            binding.bottomNavigationView.menu.getItem(i).isChecked = false
        }


        binding.bottomNavigationView.menu.setGroupCheckable(0, true, true)
        binding.bottomNavigationView.setOnItemSelectedListener(bottomNavListener)


        // Ajustar el Navigation Drawer al 55% del ancho de la pantalla
        setDrawerWidth(binding.navView, 0.55)

        binding.btnLogout.setOnClickListener { logout() }


        // Aplica fecha y hora actual al resumen de la compra
        aplicaHoraActual()

        // Obtener el título (nombre del título de tarjeta)  desde el Intent
        val titulo = intent.getStringExtra("titulo")

        // Obtener el título de la suscripción premium desde el Intent
        val premium = intent.getStringExtra("premium")


        /*
         *   API + Título de compra +  actualizar id de tarjetaMovicard
         */


        // creo el SessionManager para poder acceder a los datos guardados del usuario
        val sessionManager = SessionManager(this)

        // creo el ViewModel usando el Factory personalizado
        val viewModelFactory = UsuarioViewModelFactory(RetrofitInstance.api, sessionManager)
        val viewModelTicket = ViewModelProvider(this, viewModelFactory).get(TicketViewModel::class.java)
        val viewModelTarjeta = ViewModelProvider(this, viewModelFactory).get(TarjetaViewModel::class.java)
        val viewModelSuscripcion = ViewModelProvider(this, viewModelFactory).get(SuscripcionViewModel::class.java)

        // Si el título no es nulo
        if (!titulo.isNullOrEmpty()) {
            binding.productTitle.text = titulo
            val idCliente = sessionManager.getCliente()?.id ?: -1

            viewModelTicket.existeTicket { existe ->
                if (existe) {
                    viewModelTicket.actualizarTicket(titulo, idCliente)
                    viewModelTicket.cargarTicket()
                } else {
                    viewModelTicket.crearTicket(titulo)
                    viewModelTicket.cargarTicket()
                }

                // IMPORTANTE: cuando el ticket esté listo, actualizamos la tarjeta
                lifecycleScope.launch {
                    delay(300) // da un pequeño tiempo a cargarTicket
                    val ticket = viewModelTicket.ticket.value
                    val ticketId = ticket?.id

                    if (ticketId != null) {
                        viewModelTarjeta.existeTarjeta { existeTarjeta ->
                            if (existeTarjeta) {
                                val tarjeta = viewModelTarjeta.tarjeta.value

                                // Si ya tiene ese ID de ticket, no hacemos nada
                                if (tarjeta?.id_ticket == ticketId) {
                                    Log.d("TarjetaViewModel", "La tarjeta ya tiene este ticket asignado.")
                                    return@existeTarjeta
                                }

                                // Si tiene un ticket distinto o null, lo actualizamos
                                viewModelTarjeta.actualizarIdTicket(ticketId)

                            } else {
                                // Si no hay tarjeta, creamos una nueva con el ticket
                                val suscripcionId = viewModelTarjeta.tarjeta.value?.id_suscripcion
                                    ?: viewModelSuscripcion.suscripcion.value?.id

                                if (suscripcionId != null) {
                                    viewModelTarjeta.crearTarjeta(
                                        idSuscripcion = suscripcionId,
                                        idTicket = ticketId
                                    )
                                } else {
                                    Log.e("TarjetaViewModel", "No se pudo obtener el ID de suscripción.")
                                }
                            }
                        }
                    }
                }
            }
        } else {
            binding.productTitle.text = premium
            val viewModelSuscripcion = ViewModelProvider(this, viewModelFactory).get(SuscripcionViewModel::class.java)

            viewModelSuscripcion.actualizarASuscripcionPremium()
            viewModelSuscripcion.cargarSuscripcion()
        }

        // Obtengo el precio final de la tarjeta comprada desde el Intent
        val precio = intent.getStringExtra("precio") ?: ""

        // Si el precio != null, actualizo el TextView
        if (!titulo.isNullOrEmpty()) {
            binding.productPrice.text = precio
        }


        // Obtengo los valores de PaymentDetails y los agrego en esta clase
        intent.extras?.apply {
            binding.nombre.setText(getString("nombre"))
            binding.apellido.setText(getString("apellido"))
            binding.correo.setText(getString("correo"))
            binding.telefono.setText(getString("telefono"))
            binding.direccion.setText(getString("direccion"))
            binding.localidad.setText(getString("localidad"))
        }

        // Interacción al asignar un grado de satifacción con el RatingBar
        binding.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            Toast.makeText(this,
                getString(R.string.gracias_por_valorar_con, rating.toString()), Toast.LENGTH_SHORT).show()
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

    private fun aplicaHoraActual(){
        // Obtener la fecha y hora actual
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val fechaHoraActual = sdf.format(Date())

        // Asignarla al TextView
        binding.fechayhora.text = fechaHoraActual
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
                startActivity(Intent(this, TarjetaUUID::class.java))
                return true
            }
        }
        return false
    }
}