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
    private var origen: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegistraTarjetaBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        drawerLayout = binding.drawerLayoutRegistraTarjeta
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

        binding.navView.setNavigationItemSelectedListener(navMenuListener)
        binding.bottomNavigationView.setOnItemSelectedListener(bottomNavListener)

        binding.btnLogout.setOnClickListener { logout() }


        // Recibir el origen de la activity previa
        origen = intent.getStringExtra("origen")
        binding.registraTarjeta.setOnClickListener {
            val bundle = intent.extras
            val intent = Intent(this, AnimationRegisterCard::class.java)
            intent.putExtra("origen", origen) // Se pasa el origen recibido correctamente
            if (bundle != null) {
                intent.putExtras(bundle)
            }
            startActivity(intent)
        }

        setDrawerWidth(binding.navView, 0.55)
        setupCardInputs() // 📌 Aplicar validaciones
    }

    // Mueve la función validateFields fuera de setupCardInputs
    private fun validateFields() {
        val numeroTarjetaText = binding.numeroTarjeta.text.toString().replace(" ", "")
        val vencimientoText = binding.vencimiento.text.toString()
        val cvvText = binding.cvv.text.toString()

        var isValid = true

        // Validar número de tarjeta
        if (numeroTarjetaText.length != 16 || !numeroTarjetaText.matches(Regex("\\d+"))) {
            binding.numeroTarjeta.error = "Número de tarjeta inválido"
            isValid = false
        }

        // Validar fecha de vencimiento (MM/AA)
        if (vencimientoText.length == 5) {
            val mes = vencimientoText.substring(0, 2).toIntOrNull() ?: 0
            val anio = vencimientoText.substring(3, 5).toIntOrNull()?.plus(2000) ?: 0

            // Obtener la fecha actual
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            val currentMonth = java.util.Calendar.getInstance()
                .get(java.util.Calendar.MONTH) + 1 // Meses comienzan desde 0

            // Validación: si la fecha es mayor a 10 años
            if (anio > currentYear + 10) {
                binding.vencimiento.error =
                    "La tarjeta tiene una fecha de vencimiento mayor a 10 años"
                isValid = false
            } else if (anio == currentYear && mes < currentMonth) {
                binding.vencimiento.error = "La tarjeta tiene una fecha de vencimiento anterior"
                isValid = false
            } else if (anio < currentYear || mes !in 1..12) {
                binding.vencimiento.error = "Fecha inválida"
                isValid = false
            }
        }

        // Validar CVV
        if (cvvText.length != 3) {
            binding.cvv.error = "CVV inválido"
            isValid = false
        }

        // Log de depuración para verificar el estado de isValid
        println("isValid: $isValid") // Verifica si el estado es válido o no

        // Habilitar o deshabilitar el botón según la validez de los campos
        binding.registraTarjeta.isEnabled = isValid
    }



    private fun setupCardInputs() {
        val numeroTarjeta = binding.numeroTarjeta
        val vencimiento = binding.vencimiento
        val cvv = binding.cvv
        val btnRegistraTarjeta = binding.registraTarjeta  // Botón que cambia de actividad

        // Formateo del número de tarjeta (XXXX XXXX XXXX XXXX)
        numeroTarjeta.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateFields()
            }

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

        // Validación del vencimiento (MM/AA)
        vencimiento.addTextChangedListener(object : TextWatcher {
            private var isEditing = false

            override fun afterTextChanged(s: Editable?) {
                validateFields() // Ahora funciona porque validateFields es un miembro de la clase
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isEditing || s.isNullOrEmpty()) return
                isEditing = true

                var cleanText = s.toString().replace("/", "").take(4) // Permitir hasta 4 dígitos numéricos

                // Validar si es un número
                if (!cleanText.matches(Regex("\\d*"))) {
                    vencimiento.error = "Solo números"
                }

                // Agregar "/" después de dos dígitos si no existe
                if (cleanText.length > 2) {
                    cleanText = cleanText.substring(0, 2) + "/" + cleanText.substring(2)
                }

                // Actualizar campo sin bucle infinito
                if (cleanText != s.toString()) {
                    vencimiento.setText(cleanText)
                    vencimiento.setSelection(cleanText.length)
                }

                isEditing = false
            }
        })

        // Solo permite 3 dígitos en CVV
        cvv.filters = arrayOf(InputFilter.LengthFilter(3))
        cvv.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateFields() // Ahora funciona porque validateFields es un miembro de la clase
            }

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
