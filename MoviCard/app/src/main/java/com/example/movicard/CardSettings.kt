package com.example.movicard

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.movicard.databinding.ActivityCardSettingsBinding
import com.example.movicard.helper.SessionManager
import com.example.movicard.model.viewmodel.SuscripcionViewModel
import com.example.movicard.model.viewmodel.TarjetaViewModel
import com.example.movicard.model.viewmodel.UsuarioViewModelFactory
import com.example.movicard.network.RetrofitInstanceAPI
import com.google.android.material.navigation.NavigationView

class CardSettings : AppCompatActivity() {
    private lateinit var binding: ActivityCardSettingsBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar la Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)


        // Configurar el menú lateral
        drawerLayout = binding.drawerLayoutCardSettings
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

        binding.bloqueartarjeta.setOnClickListener {
            startActivity(Intent(this, BlockCard::class.java))
        }

        binding.recargaSaldo.setOnClickListener {
            startActivity(Intent(this, ConsultaSaldo::class.java))
        }

        val sessionManager = SessionManager(this)

        val viewModelFactory = UsuarioViewModelFactory(RetrofitInstanceAPI.api, sessionManager)
        val viewModelSuscripcion = ViewModelProvider(this, viewModelFactory).get(SuscripcionViewModel::class.java)
        binding.cancelarSuscripion.setOnClickListener {
            viewModelSuscripcion.verificaSuscripcionGratuita { esGratuita ->
                if (esGratuita) {
                    showToast(getString(R.string.a_n_no_dispone_de_una_suscripci_n))
                } else {
                    alertDialogCancelarSuscipcion(viewModelSuscripcion)
                }
            }
        }
    }

    private fun alertDialogCancelarSuscipcion(viewModelSuscripcion : SuscripcionViewModel){
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.cancelar_plan_premium))
            .setMessage(getString(R.string.est_totalmente_seguro_de_que_quiere_cancelar_la_suscripci_n))
            .setPositiveButton(getString(R.string.confirmar)) { _, _ ->
                viewModelSuscripcion.actualizarASuscripcionGratuita()
                viewModelSuscripcion.cargarSuscripcion()
                showToast(getString(R.string.cambio_de_plan_actualizado_con_xito))
            }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
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
        }
        return false
    }
}