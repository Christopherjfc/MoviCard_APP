package com.example.movicard

import android.content.Intent
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.DisplayMetrics
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.movicard.databinding.ActivityPerfilUsuarioBinding
import com.example.movicard.helper.SessionManager
import com.example.movicard.model.viewmodel.ClienteViewModel
import com.example.movicard.model.viewmodel.SuscripcionViewModel
import com.example.movicard.model.viewmodel.UsuarioViewModelFactory
import com.example.movicard.network.RetrofitInstance
import com.google.android.material.navigation.NavigationView
import androidx.core.view.isGone
import com.example.movicard.model.viewmodel.TarjetaViewModel

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
        val viewModelFactory = UsuarioViewModelFactory(RetrofitInstance.api, sessionManager)
        val viewModelCliente = ViewModelProvider(this, viewModelFactory).get(ClienteViewModel::class.java)
        val viewModelSuscripcion = ViewModelProvider(this, viewModelFactory).get(SuscripcionViewModel::class.java)
        val viewModelTarjeta = ViewModelProvider(this, viewModelFactory).get(TarjetaViewModel::class.java)

        // observo el LiveData del cliente y actualizo la UI cuando llegue la respuesta
        viewModelCliente.cliente.observe(this) { cliente ->
            // actualizo los campos de la interfaz con los datos del cliente
            binding.nombrePerfil.setText(cliente.nombre + " " + cliente.apellido)
            binding.correo.setText(cliente.correo)
            binding.nuevoNombre.setText(cliente.nombre)
            binding.nuevoApellido.setText(cliente.apellido)
            binding.nuevoDNI.setText(cliente.dni)
            binding.nuevoCorreo.setText(cliente.correo)
            binding.nuevoTelefono.setText(cliente.telefono)
            binding.nuevaDireccion.setText(cliente.direccion)
            binding.nuevoNPiso.setText(cliente.numero_piso)
            binding.nuevoNBloque.setText(cliente.numero_bloque)
            binding.nuevoCodigoPostal.setText(cliente.codigopostal)
            binding.nuevaCiudad.setText(cliente.ciudad)
        }

        viewModelSuscripcion.suscripcion.observe(this) { suscripcion ->
            binding.textSuscripcion.setText(suscripcion.suscripcion)
            viewModelTarjeta.crearTarjeta(suscripcion.id)
        }

        viewModelTarjeta.tarjeta.observe(this) { tarjeta ->
            binding.textUUIDTarjeta.setText(tarjeta?.estadotarjeta)
        }

        // Llamamos a la función para iniciar la carga de datos del cliente
        viewModelCliente.cargarCliente()

        viewModelSuscripcion.cargarSuscripcion()

        viewModelTarjeta.cargarTarjeta()


        // Botones para guardar o cancelar el cambio de datos personales
        binding.btnGuardarDatos.setOnClickListener {
            val clienteActual = sessionManager.getCliente()

            val nombre = binding.nuevoNombre.text.toString().trim()
            val apellido = binding.nuevoApellido.text.toString().trim()
            val dni = binding.nuevoDNI.text.toString().trim()
            val correo = binding.nuevoCorreo.text.toString().trim()
            val telefono = binding.nuevoTelefono.text.toString().trim()
            val direccion = binding.nuevaDireccion.text.toString().trim()
            val numeroPiso = binding.nuevoNPiso.text.toString().trim()
            val numeroBloque = binding.nuevoNBloque.text.toString().trim()
            val codigoPostal = binding.nuevoCodigoPostal.text.toString().trim()
            val ciudad = binding.nuevaCiudad.text.toString().trim()

            // Validaciones previas

            // 1. Comprobamos campos vacíos obligatorios (excepto número de piso)
            if (nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty() || correo.isEmpty() ||
                telefono.isEmpty() || direccion.isEmpty() || numeroBloque.isEmpty() ||
                codigoPostal.isEmpty() || ciudad.isEmpty()
            ) {
                showToast("Todos los campos deben estar completos (excepto el piso)")
                return@setOnClickListener
            }

            // 2. Validación de formato del DNI español
            if (!dni.matches(Regex("^[XYZ]?[0-9]{7,8}[A-Za-z]$"))) {
                binding.nuevoDNI.error = "DNI inválido. Debe tener 8 números y 1 letra."
                return@setOnClickListener
            }

            // 3. Validación del código postal: 5 dígitos numéricos
            if (!codigoPostal.matches(Regex("^\\d{5}$"))) {
                binding.nuevoCodigoPostal.error = "Código postal inválido (debe tener 5 dígitos)"
                return@setOnClickListener
            }

            if (clienteActual != null) {

                // Creo un objeto cliente actualizado
                val clienteActualizado = clienteActual.copy(
                    nombre = nombre,
                    apellido = apellido,
                    dni = dni,
                    correo = correo,
                    telefono = telefono,
                    direccion = direccion,
                    numero_piso = if (numeroPiso.isEmpty()) null else numeroPiso,
                    numero_bloque = numeroBloque,
                    codigopostal = codigoPostal,
                    ciudad = ciudad,
                    password = clienteActual.password
                )
                viewModelCliente.actualizarDatosCliente(clienteActualizado)
                viewModelCliente.cargarCliente()

                showToast("Datos actualizados correctamente")
                toggleVisibilityCambiarDatos()
            }
        }

        binding.btnCancelarDatos.setOnClickListener {
            toggleVisibilityCambiarDatos()
        }


        // Botones para guardar o cancelar el cambio de contraseña
        binding.btnGuardarContra.setOnClickListener {
            val clienteActual = sessionManager.getCliente()
            val actual = binding.contraActual.text.toString()

            if (clienteActual != null && clienteActual.password.toString() != actual) {
                binding.contraActual.error = "La contraseña no coincide"
            }

            val nueva = binding.nuevaContra.text.toString()
            val repetir = binding.repiteNuevaContra.text.toString()

            if (repetir != nueva) {
                binding.repiteNuevaContra.error = "La contraseña de confirmación no coincide con la nueva"
                return@setOnClickListener
            }

            viewModelCliente.cambiarPassword(actual, nueva,
                onSuccess = {
                    toggleVisibilityCambiarContra()
                    showToast("Contraseña actualizada con éxito")
                    limpiaCamposCambiarContra()
                },
                onError = {
                    print("Error al cambiar la contraseña: $it")
                }
            )
            viewModelCliente.cargarCliente()
        }

        binding.btnCancelarContra.setOnClickListener {
            toggleVisibilityCambiarContra()
            limpiaCamposCambiarContra()
        }

    }

    // limpia los campos al cancelar o guardar el cambio de conraseña
    private fun limpiaCamposCambiarContra() {
        binding.contraActual.text.clear()
        binding.nuevaContra.text.clear()
        binding.repiteNuevaContra.text.clear()
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    private fun toggleVisibilityCambiarContra() {
        // se despliega cuando se le da clic y no está visible el cardView
        if (binding.cardCambiarContra.isGone) {
            TransitionManager.beginDelayedTransition(binding.cardCambiarContra, AutoTransition())
            binding.cardCambiarContra.visibility = View.VISIBLE
            binding.cardSuscripcion.visibility = View.GONE
            binding.cardUUIDTarjeta.visibility = View.GONE
            binding.btnDatosPersonales.visibility = View.GONE
            binding.desplegarContra.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.flecha_abajo, 0)
        } else {
            // se vuelve invisible cuando se le da clic y está visible el cardView
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