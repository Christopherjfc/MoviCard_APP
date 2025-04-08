package com.example.movicard

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.movicard.databinding.ActivityGraficasBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.navigation.NavigationView

class Graficas : BaseActivity() {
    private lateinit var binding: ActivityGraficasBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGraficasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar la Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)


        // Configurar el menú lateral
        drawerLayout = binding.drawerLayoutGraficas
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


        // Le paso la ID de la grafica Pie al method
        // que la llenará
        llenarGraficaPie(binding.graficaPie)

        val prefs = getSharedPreferences("TarjetasPrefs", Context.MODE_PRIVATE)
        val gastoTotalCentavos = prefs.getInt("gasto_total", 0)
        val gastoTotalEuros = gastoTotalCentavos / 100.0

        // Asigno los gastos totales a la UI
        binding.gastosTotales.text = " %.2f €".format(gastoTotalEuros)
    }


    fun llenarGraficaPie(graficaPie: PieChart) {
        val tarjetas = TarjetaStorage.cargarTarjetas(this)

        val conteo = mapOf(
            "MOVI_10" to tarjetas.count { it.nombre == "TENMOVI" }.toFloat(),
            "MOVI_MES" to tarjetas.count { it.nombre == "MOVIMES" }.toFloat(),
            "MOVI_TRIMESTRAL" to tarjetas.count { it.nombre == "TRIMOVI" }.toFloat(),
            "SUSCRIPCIÓN PREMIUM" to tarjetas.count { it.nombre == "SUSCRIPCIÓN PREMIUM" }.toFloat()
        )

        val valores = ArrayList<PieEntry>()
        valores.add(PieEntry(conteo["MOVI_10"] ?: 0f, "TENMOVI"))
        valores.add(PieEntry(conteo["MOVI_MES"] ?: 0f, "MOVIMES"))
        valores.add(PieEntry(conteo["MOVI_TRIMESTRAL"] ?: 0f, "TRIMOVI"))
        valores.add(PieEntry(conteo["SUSCRIPCIÓN PREMIUM"] ?: 0f, "SUSCRIPCIÓN PREMIUM"))

        val conjuntoDeDatos = PieDataSet(valores, "Tarjetas Vendidas")
        conjuntoDeDatos.colors = listOf(
            Color.rgb(88, 214, 141), // verde (10)
            Color.rgb(93, 173, 226), // azul (mes)
            Color.rgb(241, 148, 138), // rojo (trimestral)
            Color.rgb(178, 144, 232)) // morado lavanda claro (premium)

        conjuntoDeDatos.valueTextColor = Color.BLACK
        conjuntoDeDatos.valueTextSize = 16f

        binding.graficaPie.setEntryLabelColor(Color.BLACK) // Color de les etiquetes
        binding.graficaPie.setEntryLabelTextSize(14f) // Mida del text de les etiquetes

        val data = PieData(conjuntoDeDatos)
        graficaPie.data = data
        graficaPie.animateY(1000)
        graficaPie.description.isEnabled = false
        graficaPie.invalidate()
    }




    // Method para ajustar el ancho del Navigation Drawer basado en un porcentaje de la pantalla
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

//    fun llenarGraficaBarra(graficaBarra: BarChart) {
//        // Datos de ejemplo para la gráfica de barras
//        val valores = ArrayList<BarEntry>()
//        valores.add(BarEntry(0f, 15f)) // Barra 1
//        valores.add(BarEntry(1f, 30f)) // Barra 2
//        valores.add(BarEntry(2f, 50f)) // Barra 3
//        valores.add(BarEntry(3f, 25f)) // Barra 4
//        valores.add(BarEntry(4f, 40f)) // Barra 5
//
//        // Crear un conjunto de datos de barras
//        val conjuntoDeDatos = BarDataSet(valores, "Valores de Ejemplo")
//
//        // Establecer colores para las barras
//        conjuntoDeDatos.colors = listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN)
//
//        // Crear los datos para la gráfica
//        val data = BarData(conjuntoDeDatos)
//
//        // Asignar los datos a la gráfica
//        graficaBarra.data = data
//        graficaBarra.animateY(1000)
//        graficaBarra.description.isEnabled = false
//        graficaBarra.invalidate() // Refresca la gráfica
//    }
