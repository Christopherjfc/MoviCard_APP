package com.example.movicard

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.movicard.databinding.ActivityGraficasBinding
import com.example.movicard.databinding.ActivityPaymentDetailsBinding
import com.google.android.material.navigation.NavigationView

class PaymentDetails : BaseActivity() {
    private lateinit var binding: ActivityPaymentDetailsBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar la Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)


        // Configurar el menú lateral
        drawerLayout = binding.drawerLayoutPaymentDetails
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

        // Obtener el título desde el Intent con el que se abrió PaymentDetails
        val titulo = intent.getStringExtra("titulo")

        // Validar y modificar el TextView si es necesario
        val nuevoTitulo = when (titulo) {
            "MOVI_10" -> "MOVI_10"
            "MOVI_MES" -> "MOVI_MES"
            "MOVI_TRIMESTRAL" -> "MOVI_TRIMESTRAL"
            else -> "Suscripción premium" // Si no coincide, usa el original
        }

        // Actualizar el TextView solo si el nuevo título no es nulo o vacío
        binding.productTitle.text = nuevoTitulo ?: ""

        // cambios de precio según el título de la tarjeta o suscripción
        when (titulo) {
            "MOVI_10" -> {
                binding.productPrice.text = "12.55 €"
                binding.subtotal.text =  "12.55 €"
                binding.productPrice2.text = "13.80 €"
                binding.gastosGestion.text = "1.25 €"
            }
            "MOVI_MES" -> {
                binding.productPrice.text = "22 €"
                binding.subtotal.text =  "22 €"
                binding.productPrice2.text = "24.20 €"
                binding.gastosGestion.text = "2.2 €"
            }
            "MOVI_TRIMESTRAL" -> {
                binding.productPrice.text = "44 €"
                binding.subtotal.text =  "44 €"
                binding.productPrice2.text = "48.40 €"
                binding.gastosGestion.text = "4.4 €"
            } else -> {
                binding.linearGestion.visibility = View.GONE
            }
        }

        // Click en botón de suscripción
        binding.continuar.setOnClickListener {
            val intent = Intent(this, PaymentByCard::class.java)
            intent.putExtra("titulo", titulo)  // Volver a enviarlo con la misma clave
            intent.putExtra("precio", binding.productPrice2.text.toString())
            intent.putExtra("origen", "PrincingCards")

            val bundle = Bundle().apply {
                putString("nombre", binding.nombreEnvio.text.toString())
                putString("apellido", binding.apellidoEnvio.text.toString())
                putString("correo", binding.correoEnvio.text.toString())
                putString("telefono", binding.telefonoEnvio.text.toString())
                putString("direccion", binding.direccionEnvio.text.toString())
                putString("localidad", binding.localidadEnvio.text.toString())
            }
            intent.putExtras(bundle)
            startActivity(intent)
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
                startActivity(Intent(this, TarjetaUUID::class.java))
                return true
            }
        }
        return false
    }
}