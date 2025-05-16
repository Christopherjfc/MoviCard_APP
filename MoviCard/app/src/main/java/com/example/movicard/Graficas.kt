package com.example.movicard

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.movicard.databinding.ActivityGraficasBinding
import com.example.movicard.helper.SessionManager
import com.example.movicard.model.viewmodel.ClienteViewModel
import com.example.movicard.model.viewmodel.UsuarioViewModelFactory
import com.example.movicard.network.RetrofitInstanceAPI
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

        val sessionManager = SessionManager(this)

        // creo el ViewModel usando el Factory personalizado
        val viewModelFactory = UsuarioViewModelFactory(RetrofitInstanceAPI.api, sessionManager)
        val viewModelcliente = ViewModelProvider(this, viewModelFactory).get(ClienteViewModel::class.java)

        // obtengo el menu drawe y busco su textView para sustituirlo
        val headerView = binding.navView.getHeaderView(0)
        val nombreMenuDrawer = headerView.findViewById<TextView>(R.id.nombre)

        // observo el LiveData del cliente
        viewModelcliente.cliente.observe(this) { cliente ->
            // actualizo el nombre del menu drawer con el usuario actual
            nombreMenuDrawer.text = cliente.nombre + " " + cliente.apellido
        }

        // actualizamos el observe con los nuevos datos
        viewModelcliente.cargarCliente()

        // bloque 1

        // Le paso la ID de la grafica Pie al method que la llenará
        llenarGraficaPie(binding.graficaPie)

        // Inicio ñla grafica de barras
        llenarGraficaBarra(binding.graficaBarra)

        val prefs = getSharedPreferences("TarjetasPrefs", Context.MODE_PRIVATE)
        val gastoTotalCentavos = prefs.getInt("gasto_total", 0)
        val gastoTotalEuros = gastoTotalCentavos / 100.0

        // Asigno los gastos totales a la UI
        binding.gastosTotales.text = " %.2f €".format(gastoTotalEuros)
    }


    // bloque 2
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

        val conjuntoDeDatos = PieDataSet(valores, getString(R.string.tarjetas_vendidas))
        conjuntoDeDatos.colors = listOf(
            Color.rgb(88, 214, 141), // verde (10)
            Color.rgb(93, 173, 226), // azul (mes)
            Color.rgb(241, 148, 138), // rojo (trimestral)
            Color.rgb(178, 144, 232)) // morado lavanda claro (premium)

        conjuntoDeDatos.valueTextColor = Color.BLACK
        conjuntoDeDatos.valueTextSize = 16f

        binding.graficaPie.setEntryLabelColor(Color.BLACK) // Color de les etiquetes
        binding.graficaPie.setEntryLabelTextSize(12f) // Mida del text de les etiquetes

        val data = PieData(conjuntoDeDatos)
        graficaPie.data = data
        graficaPie.animateY(1000)
        graficaPie.invalidate()

        // cambia el color del texto de la leyenda
        graficaPie.legend.textColor = ContextCompat.getColor(this, R.color.text_primary)

        graficaPie.description.isEnabled = false
    }


    // bloque 3
    fun llenarGraficaBarra(graficaBarra: BarChart) {
        val prefs = getSharedPreferences("TarjetasPrefs", Context.MODE_PRIVATE)
        val gastoPorDiaJson = prefs.getString("gastos_por_dia", "{}") ?: "{}"

        val gastoMap: Map<String, Int> = Gson().fromJson(
            gastoPorDiaJson,
            object : com.google.gson.reflect.TypeToken<Map<String, Int>>() {}.type
        ) ?: emptyMap()

        // Obtener los días del mes actual
        val calendar = Calendar.getInstance()
        val mesActual = calendar.get(Calendar.MONTH)
        val añoActual = calendar.get(Calendar.YEAR)
        val totalDias = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        for (dia in 1..totalDias) {
            calendar.set(Calendar.DAY_OF_MONTH, dia)
            val fecha = formatoFecha.format(calendar.time)

            val gastoCentavos = gastoMap[fecha] ?: 0
            val gastoEuros = gastoCentavos / 100f

            entries.add(BarEntry(dia.toFloat(), gastoEuros))
            labels.add(dia.toString())
        }

        val dataSet = BarDataSet(entries, getString(R.string.gastos_por_d_a))
        dataSet.color = Color.rgb(0, 123, 255)
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = ContextCompat.getColor(this, R.color.text_primary)

        val barData = BarData(dataSet)
        graficaBarra.data = barData

        graficaBarra.description.isEnabled = false
        graficaBarra.animateY(1000)

        // cambia el color del texto de la leyenda
        graficaBarra.legend.textColor = ContextCompat.getColor(this, R.color.text_primary)

        // cambia el color del texto de los ejes
        graficaBarra.xAxis.textColor = ContextCompat.getColor(this, R.color.text_primary)
        graficaBarra.axisLeft.textColor = ContextCompat.getColor(this, R.color.text_primary)
        graficaBarra.axisRight.textColor = ContextCompat.getColor(this, R.color.text_primary)
        graficaBarra.invalidate()
    }


    // bloque 4
    // inserta datos de prueba en la grafica de barras para ahorrar tiempo
    fun insertarDatosDePrueba(view: View) {
        val prefs = getSharedPreferences("TarjetasPrefs", Context.MODE_PRIVATE)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val gastos = mutableMapOf<String, Int>()

        for (i in 1..30) {
            calendar.set(Calendar.DAY_OF_MONTH, i)
            val fecha = sdf.format(calendar.time)
            gastos[fecha] = (1500..16000).random() // entre $5.00 y $20.00
        }

        prefs.edit().putString("gastos_por_dia", Gson().toJson(gastos)).apply()

        Toast.makeText(this, getString(R.string.datos_de_prueba_insertados), Toast.LENGTH_SHORT).show()

        // Opcional: actualizar la gráfica automáticamente
        llenarGraficaBarra(binding.graficaBarra)
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
                startActivity(Intent(this, BlockCard::class.java))
                return true
            }
        }
        return false
    }
}

