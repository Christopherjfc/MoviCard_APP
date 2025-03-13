package com.example.movicard

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.movicard.databinding.ActivityHelpBinding
import com.google.android.material.navigation.NavigationView

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
        val consultas = listOf(getString(R.string.consulta_general),
            getString(R.string.problemas_con_la_tarjeta),
            getString(R.string.errores_en_el_saldo_o_recargas),
            getString(R.string.sugerencias_y_mejoras), getString(R.string.otra_consulta))
        configurarSpinner(binding.tipoConsulta, consultas)

        // botón que comprueba y envía el mensaje
        binding.btnEnviarMensaje.setOnClickListener {
            if (mensajeValido()) {
                Toast.makeText(this, "Mensaje enviado.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            }
        }


        // Ajustar el Navigation Drawer al 55% del ancho de la pantalla
        setDrawerWidth(binding.navView, 0.55)
    }

    // valida que no haya ningún campo vacío para que sea válido el mensaje
    private fun mensajeValido(): Boolean {
        val nombre = binding.nombreUsuario.text.toString().trim()
        val correo = binding.correoUsuario.text.toString().trim()
        val mensaje = binding.mensajeUsuario.text.toString().trim()

        return (nombre.isNotEmpty() && correo.isNotEmpty() && mensaje.isNotEmpty())
    }

    private fun configurarSpinner(spinner: Spinner, opciones: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    val seleccionado = parent.getItemAtPosition(position).toString()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
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

    // Listener para los elementos del MENÚ INFERIOR
    private val bottomNavListener = fun(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                startActivity(Intent(this, Principal::class.java))
                return true
            }
            R.id.tarjeta -> {
                startActivity(Intent(this, TarjetaUUID::class.java))
                return true
            }
        }
        return false
    }
}