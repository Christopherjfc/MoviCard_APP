package com.example.movicard

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.movicard.databinding.ActivityRegistraTarjetaBinding
import com.google.android.material.navigation.NavigationView

class RegistraTarjeta : BaseActivity() {
    private lateinit var binding: ActivityRegistraTarjetaBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegistraTarjetaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        drawerLayout = binding.drawerLayoutRegistraTarjeta
        toggle = ActionBarDrawerToggle(this, drawerLayout, binding.toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val iconColor = resources.getColor(R.color.white, theme)
        toggle.drawerArrowDrawable.color = iconColor

        binding.navView.setNavigationItemSelectedListener(navMenuListener)
        binding.bottomNavigationView.setOnItemSelectedListener(bottomNavListener)

        binding.btnLogout.setOnClickListener { logout() }
        binding.registraTarjeta.setOnClickListener {
            startActivity(Intent(this, AnimationRegisterCard::class.java))
        }

        setDrawerWidth(binding.navView, 0.55)
        setupCardInputs() // 📌 Aplicar validaciones
    }

    private fun setupCardInputs() {
        val numeroTarjeta = binding.numeroTarjeta
        val vencimiento = binding.vencimiento
        val cvv = binding.cvv

        // Formateo del número de tarjeta (XXXX XXXX XXXX XXXX)
        numeroTarjeta.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    val cleanText = it.toString().replace(" ", "").take(16)
                    if (!cleanText.matches(Regex("\\d*"))) {
                        numeroTarjeta.error = "Solo números"
                    }
                    val formatted = cleanText.chunked(4).joinToString(" ")
                    if (formatted != it.toString()) {
                        numeroTarjeta.setText(formatted)
                        numeroTarjeta.setSelection(formatted.length)
                    }
                }
            }
        })

        // Formato de vencimiento (MM/AA)
        vencimiento.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    var cleanText = it.toString().replace("/", "")
                    if (cleanText.length > 4) cleanText = cleanText.take(4)

                    // Validar si es un número
                    if (!cleanText.matches(Regex("\\d*"))) {
                        vencimiento.error = "Solo números"
                    }

                    // Agregar un "0" al mes si es un solo dígito
                    if (cleanText.length == 1 && cleanText.toInt() in 1..9) {
                        cleanText = "0$cleanText"
                    }

                    // Agregar "/" después de dos dígitos
                    if (cleanText.length == 2 && before == 0 && !it.contains("/")) {
                        cleanText += "/"
                    }

                    // Validar fecha
                    if (cleanText.length == 5) {
                        val mes = cleanText.substring(0, 2).toIntOrNull() ?: 0
                        val anio = cleanText.substring(3, 5).toIntOrNull()?.plus(2000) ?: 0

                        if (mes !in 1..12 || anio < 2025) {
                            vencimiento.error = "Fecha inválida"
                        }
                    }

                    if (cleanText != it.toString()) {
                        vencimiento.setText(cleanText)

                        // ✅ Evita el error verificando que la longitud es válida antes de `setSelection()`
                        if (cleanText.length <= vencimiento.text.length) {
                            vencimiento.setSelection(cleanText.length)
                        }
                    }
                }
            }
        })

        // Solo permite 3 dígitos en CVV
        cvv.filters = arrayOf(InputFilter.LengthFilter(3))
        cvv.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    if (!it.toString().matches(Regex("\\d*"))) {
                        cvv.error = "Solo números"
                    }
                }
            }
        })
    }


    private fun setDrawerWidth(navView: NavigationView, percentage: Double) {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val drawerWidth = (screenWidth * percentage).toInt()
        val layoutParams = navView.layoutParams
        layoutParams.width = drawerWidth
        navView.layoutParams = layoutParams
    }

    private val navMenuListener = NavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_profile -> startActivity(Intent(this, PerfilUsuario::class.java))
            R.id.nav_suscription -> startActivity(Intent(this, PricingCards::class.java))
            R.id.nav_config -> startActivity(Intent(this, Settings::class.java))
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        true
    }

    private fun logout() {
        startActivity(Intent(this, Login::class.java))
        finish()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressedDispatcher
        }
    }

    private val bottomNavListener = fun(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.help -> {
                startActivity(Intent(this, Help::class.java))
                return true
            }
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
