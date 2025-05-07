package com.example.movicard

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.movicard.databinding.ActivityHelpBinding
import com.example.movicard.email.GmailSender
import com.example.movicard.helper.SessionManager
import com.example.movicard.model.viewmodel.ClienteViewModel
import com.example.movicard.model.viewmodel.TarjetaViewModel
import com.example.movicard.model.viewmodel.UsuarioViewModelFactory
import com.example.movicard.network.RetrofitInstanceAPI
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.*

class Help : BaseActivity() {

    private lateinit var binding: ActivityHelpBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar la Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Configurar el menú lateral
        drawerLayout = binding.drawerLayoutHelp
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, binding.toolbar,
            R.string.open_nav, R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val iconColor = resources.getColor(R.color.white, theme)
        toggle.drawerArrowDrawable.color = iconColor

        // Configurar la navegación LATERAL
        binding.navView.setNavigationItemSelectedListener(navMenuListener)


        // Manejar el clic en los botones de la parte inferior
        binding.bottomNavigationView.selectedItemId = R.id.help
        binding.bottomNavigationView.setOnItemSelectedListener(bottomNavListener)

        binding.btnLogout.setOnClickListener { logout() }

        // selecciona el tipo de consulta
        val consultas = listOf(
            getString(R.string.consulta_general),
            getString(R.string.problemas_con_la_tarjeta),
            getString(R.string.errores_en_el_saldo_o_recargas),
            getString(R.string.sugerencias_y_mejoras), getString(R.string.otra_consulta)
        )
        configurarSpinner(binding.tipoConsulta, consultas)

        // botón que comprueba y envía el mensaje
        binding.btnEnviarMensaje.setOnClickListener {
            if (mensajeValido()) {
                val nombre = binding.nombreUsuario.text.toString().trim()
                val mensaje = binding.mensajeUsuario.text.toString().trim()
                val tipoConsulta = binding.tipoConsulta.selectedItem.toString()

                val asunto = "Consulta: $tipoConsulta"
                val cuerpo = """
                    Nombre: $nombre
                    
                    Mensaje:
                    $mensaje
                """.trimIndent()

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val sender = GmailSender(
                            "christopherjfc2@gmail.com", // <-- reemplázalo con tu cuenta
                            "evihbojhtbgrmuln"       // <-- reemplázalo con tu clave de aplicación
                        )
                        sender.sendMail(asunto, cuerpo, "2025.movicard@gmail.com")

                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@Help,
                                "Mensaje enviado correctamente.",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.mensajeUsuario.setText("") // Limpiar campo mensaje si quieres
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@Help,
                                "Error al enviar el mensaje: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT)
                    .show()
            }
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
        val viewModelFactory = UsuarioViewModelFactory(RetrofitInstanceAPI.api, sessionManager)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(ClienteViewModel::class.java)

        // observo el LiveData del cliente y actualizo la UI cuando llegue la respuesta
        viewModel.cliente.observe(this) { cliente ->
            // actualizo los campos de la interfaz con los datos del cliente
            binding.nombreUsuario.setText(cliente.nombre + " " + cliente.apellido)
        }

        // Llamamos a la función para iniciar la carga de datos del cliente
        viewModel.cargarCliente()
    }

    // valida que no haya ningún campo vacío para que sea válido el mensaje
    private fun mensajeValido(): Boolean {
        val nombre = binding.nombreUsuario.text.toString().trim()
        val mensaje = binding.mensajeUsuario.text.toString().trim()

        return (nombre.isNotEmpty() && mensaje.isNotEmpty())
    }

    private fun configurarSpinner(spinner: Spinner, opciones: List<String>) {
        val adapter =
            object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, opciones) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    (view as TextView).setTextColor(
                        resources.getColor(
                            R.color.text_cambiar_contra,
                            theme
                        )
                    ) // Establecer color aquí
                    return view
                }

                override fun getDropDownView(
                    position: Int,
                    convertView: View?,
                    parent: ViewGroup
                ): View {
                    val view = super.getDropDownView(position, convertView, parent)
                    (view as TextView).setTextColor(
                        resources.getColor(
                            R.color.text_cambiar_contra,
                            theme
                        )
                    ) // Establecer color aquí también
                    return view
                }
            }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Aquí puedes obtener el texto seleccionado si lo necesitas
                if (position > 0) {
                    val seleccionado = parent.getItemAtPosition(position).toString()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
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

    private fun isTargetActivated(callback: (Boolean) -> Unit) {
        val sessionManager = SessionManager(this)
        val viewModelFactory = UsuarioViewModelFactory(RetrofitInstanceAPI.api, sessionManager)
        val viewModelTarjeta = ViewModelProvider(this, viewModelFactory).get(TarjetaViewModel::class.java)

        viewModelTarjeta.cargarTarjeta()

        viewModelTarjeta.tarjeta.observe(this) { tarjeta ->
            val activada = tarjeta?.estadoactivaciontarjeta != "DESACTIVADA"
            callback(activada)
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


    // Listener para los elementos del MENÚ LATERAL
    private val navMenuListener = NavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_profile -> {
                // Abrir perfil de usuario
                startActivity(Intent(this, PerfilUsuario::class.java))
            }

            R.id.nav_suscription -> {
                // Abrir suscripciones (Princin cards) si la tarjeta está activada, si no a tarjetaUUID
                isTargetActivated { activada ->
                    if (activada) {
                        startActivity(Intent(this, PricingCards::class.java))
                    } else {
                        mostrarDialogoTarjetaDesactivada()
                    }
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
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressedDispatcher.onBackPressed()
        }
    }

    // Listener para los elementos del MENÚ INFERIOR
    private val bottomNavListener = fun(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                startActivity(Intent(this, Principal::class.java))
                return true
            }

            R.id.tarjeta -> {
                // Cambia a CardSettings si la tarjeta está activada, si no a TarejetaUUID
                isTargetActivated { activada ->
                        if (activada) {
                        startActivity(Intent(this, CardSettings::class.java))
                    } else {
                        mostrarDialogoTarjetaDesactivada()
                    }
                }
                return true
            }
        }
        return false
    }
}