package com.example.movicard

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movicard.data.RutaAdapter
import com.example.movicard.databinding.ActivityRoutesBinding
import com.example.movicard.model.Estacion
import com.example.movicard.model.Linea
import com.example.movicard.model.PasoEstaciones
import com.example.movicard.model.PasoTransbordo
import com.google.android.material.navigation.NavigationView

class Routes : AppCompatActivity() {
    private lateinit var binding: ActivityRoutesBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    // Lista con todas las estaciones de METRO de Barcelona
    private lateinit var estaciones: List<Estacion>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoutesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar la Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Configurar el menú lateral
        drawerLayout = binding.drawerLayoutRoutes
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

        // cargo la lsita con todas las estacones de METRO de Barcelona
        estaciones = cargarEstaciones()

        // Inicializo el AutoComplete para el origen y destino
        inicializoElAutoComplete()

        binding.btnMostrarRuta.setOnClickListener {
            val origen = binding.autoCompleteOrigen.text.toString()
            val destino = binding.autoCompleteDestino.text.toString()

            val rutaCalculada = calcularRuta(origen, destino)

            val adapter = RutaAdapter(rutaCalculada)
            binding.recyclerRuta.layoutManager = LinearLayoutManager(this)
            binding.recyclerRuta.adapter = adapter
        }

    }

    private fun inicializoElAutoComplete() {
        // Obtener sólo los nombres de las estaciones
        val nombresEstaciones = estaciones.map { it.nombre }

        // Crear un adaptador de sugerencias
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nombresEstaciones)

        // Asignarlo a los inputs
        binding.autoCompleteOrigen.setAdapter(adapter)
        binding.autoCompleteDestino.setAdapter(adapter)

        // Opcional: mostrar sugerencias inmediatamente al hacer clic
        binding.autoCompleteOrigen.setOnClickListener {
            binding.autoCompleteOrigen.showDropDown()
        }
        binding.autoCompleteDestino.setOnClickListener {
            binding.autoCompleteDestino.showDropDown()
        }
    }

    private fun cargarEstaciones(): List<Estacion> {
        // Lineas de metro
        val linea1 = Linea("L1", "#a21e06") // Roja
        val linea2 = Linea("L2", "#731f72") // Morada
        val linea3 = Linea("L3", "#055e2a") // Verde
        val linea4 = Linea("L4", "#936d00") // Amarilla
        val linea5 = Linea("L5", "#00567e") // Azul
        val linea9N = Linea("L9 N", "#A6641A") // naranja
        val linea9S = Linea("L9 S", "#A6641A") // naranja
        val linea10N = Linea("L10 N", "#197aa8") // celeste
        val linea10S = Linea("L10 S", "#197aa8") // celeste
        val linea11 = Linea("L11", "#557018") // verde claro

        return listOf(
            // L1
            Estacion("Hospital de Bellvitge", linea1),
            Estacion("Bellvitge", linea1),
            Estacion("Av. Carrilet", linea1),
            Estacion("Rbla. Just Oliveras", linea1),
            Estacion("Can Serra", linea1),
            Estacion("Florida", linea1),
            Estacion("Torrassa", linea1),
            Estacion("Santa Eulàlia", linea1),
            Estacion("Mercat Nou", linea1),
            Estacion("Plaça de Sants", linea1),
            Estacion("Hostafrancs", linea1),
            Estacion("Espanya", linea1),
            Estacion("Rocafort", linea1),
            Estacion("Urgell", linea1),
            Estacion("Universitat", linea1),
            Estacion("Catalunya", linea1),
            Estacion("Urquinaona", linea1),
            Estacion("Arc de Triomf", linea1),
            Estacion("Marina", linea1),
            Estacion("Glòries", linea1),
            Estacion("Clot", linea1),
            Estacion("Navas", linea1),
            Estacion("La Sagrera", linea1),
            Estacion("Fabra i Puig", linea1),
            Estacion("Sant Andreu", linea1),
            Estacion("Torras i Bages", linea1),
            Estacion("Trinitat Vella", linea1),
            Estacion("Baró de Viver", linea1),
            Estacion("Santa Coloma", linea1),
            Estacion("Fondo", linea1),

            // L2
            Estacion("Paral·lel", linea2),
            Estacion("Sant Antoni", linea2),
            Estacion("Universitat", linea2),
            Estacion("Passeig de Gràcia", linea2),
            Estacion("Tetuan", linea2),
            Estacion("Monumental", linea2),
            Estacion("Sagrada Família", linea2),
            Estacion("Encants", linea2),
            Estacion("Clot", linea2),
            Estacion("Bac de Roda", linea2),
            Estacion("Sant Martí", linea2),
            Estacion("La Pau", linea2),
            Estacion("Verneda", linea2),
            Estacion("Artigues | Sant Adrià", linea2),
            Estacion("Sant Roc", linea2),
            Estacion("Gorg", linea2),
            Estacion("Pep Ventura", linea2),
            Estacion("Badalona | Pompeu Fabra", linea2),

            // L3
            Estacion("Zona Universitària", linea3),
            Estacion("Palau Reial", linea3),
            Estacion("Maria Cristina", linea3),
            Estacion("Les Corts", linea3),
            Estacion("Plaça del Centre", linea3),
            Estacion("Sants Estació", linea3),
            Estacion("Tarragona", linea3),
            Estacion("Espanya", linea3),
            Estacion("Poble Sec", linea3),
            Estacion("Paral·lel", linea3),
            Estacion("Drassanes", linea3),
            Estacion("Liceu", linea3),
            Estacion("Catalunya", linea3),
            Estacion("Passeig de Gràcia", linea3),
            Estacion("Diagonal", linea3),
            Estacion("Fontana", linea3),
            Estacion("Lesseps", linea3),
            Estacion("Vallcarca", linea3),
            Estacion("Penitents", linea3),
            Estacion("Vall d'Hebron", linea3),
            Estacion("Montbau", linea3),
            Estacion("Mundet", linea3),
            Estacion("Valldaura", linea3),
            Estacion("Canyelles", linea3),
            Estacion("Roquetes", linea3),
            Estacion("Trinitat Nova", linea3),

            // L4
            Estacion("La Pau", linea4),
            Estacion("Besòs", linea4),
            Estacion("Besòs Mar", linea4),
            Estacion("El Maresme | Fòrum", linea4),
            Estacion("Selva de Mar", linea4),
            Estacion("Poblenou", linea4),
            Estacion("Llacuna", linea4),
            Estacion("Bogatell", linea4),
            Estacion("Ciutadella | Vila Olímpica", linea4),
            Estacion("Barceloneta", linea4),
            Estacion("Jaume I", linea4),
            Estacion("Urquinaona", linea4),
            Estacion("Passeig de Gràcia", linea4),
            Estacion("Girona", linea4),
            Estacion("Verdaguer", linea4),
            Estacion("Joanic", linea4),
            Estacion("Alfons X", linea4),
            Estacion("Guinardó | Hospital de Sant Pau", linea4),
            Estacion("Maragall", linea4),
            Estacion("Llucmajor", linea4),
            Estacion("Via Júlia", linea4),
            Estacion("Trinitat Nova", linea4),

            // L5
            Estacion("Cornellà Centre", linea5),
            Estacion("Gavarra", linea5),
            Estacion("Sant Ildefons", linea5),
            Estacion("Can Boixeres", linea5),
            Estacion("Can Vidalet", linea5),
            Estacion("Pubilla Cases", linea5),
            Estacion("Emest Lluch", linea5),
            Estacion("Collblanc", linea5),
            Estacion("Badal", linea5),
            Estacion("Plaça de Sants", linea5),
            Estacion("Sants Estació", linea5),
            Estacion("Entença", linea5),
            Estacion("Hospital Clínic", linea5),
            Estacion("Diagonal", linea5),
            Estacion("Verdaguer", linea5),
            Estacion("Sagrada Família", linea5),
            Estacion("Sant Pau | Dos de Maig", linea5),
            Estacion("Camp de l'Arpa", linea5),
            Estacion("La Sagrera", linea5),
            Estacion("Congrés", linea5),
            Estacion("Maragall", linea5),
            Estacion("Virrei Amat", linea5),
            Estacion("Vilapicina", linea5),
            Estacion("Horta", linea5),
            Estacion("El Carmel", linea5),
            Estacion("El Coll | La Teixonera", linea5),
            Estacion("Vall d'Hebron", linea5),

            // L9 N
            Estacion("La Sagrera", linea9N),
            Estacion("Onze de Setembre", linea9N),
            Estacion("Bon Pastor", linea9N),
            Estacion("Can Peixauet", linea9N),
            Estacion("Santa Rosa", linea9N),
            Estacion("Fondo", linea9N),
            Estacion("Església Major", linea9N),
            Estacion("Singuerlín", linea9N),
            Estacion("Can Zam", linea9N),

            // L9 S
            Estacion("Aeroport T1", linea9S),
            Estacion("Aeroport T2", linea9S),
            Estacion("Mas Blau", linea9S),
            Estacion("Parc Nou", linea9S),
            Estacion("Cèntric", linea9S),
            Estacion("El Prat Estació", linea9S),
            Estacion("Les Moreres", linea9S),
            Estacion("Mercabarna", linea9S),
            Estacion("Fira", linea9S),
            Estacion("Europa | Fira", linea9S),
            Estacion("Can Tries | Gornal", linea9S),
            Estacion("Torrassa", linea9S),
            Estacion("Collblanc", linea9S),
            Estacion("Zona Universitària", linea9S),

            // L10 N
            Estacion("La Sagrera", linea10N),
            Estacion("Onze de Setembre", linea10N),
            Estacion("Bon Pastor", linea10N),
            Estacion("Llefià", linea10N),
            Estacion("La Salut", linea10N),
            Estacion("Gorg", linea10N),

            // L10 S
            Estacion("ZAL | Riu Vell", linea10S),
            Estacion("Ecoparc", linea10S),
            Estacion("Port comercial | La Factoria", linea10S),
            Estacion("Zona Franca", linea10S),
            Estacion("Foc", linea10S),
            Estacion("Foneria", linea10S),
            Estacion("Ciutat de la Justícia", linea10S),
            Estacion("Provençana", linea10S),
            Estacion("Can Tries | Gornal", linea10S),
            Estacion("Torrassa", linea10S),
            Estacion("Collblanc", linea10S),

            // L11
            Estacion("Trinitat Nova", linea11),
            Estacion("Casa de l'Aigua", linea11),
            Estacion("Torre Baró | Vallbona", linea11),
            Estacion("Ciutat Meridiana", linea11),
            Estacion("Can Cuiàs", linea11),
        )
    }

    private fun calcularRuta(origenInput: String, destinoInput: String): List<Any> {
        val pasos = mutableListOf<Any>()

        val origen = estaciones.find { it.nombre.equals(origenInput, ignoreCase = true) }
        val destino = estaciones.find { it.nombre.equals(destinoInput, ignoreCase = true) }

        if (origen == null || destino == null) {
            showToast("Estación no encontrada.")
            return emptyList()
        }

        if (origen.linea.nombre == destino.linea.nombre) {
            val estacionesMismaLinea = estaciones.filter { it.linea.nombre == origen.linea.nombre }
            val indexOrigen = estacionesMismaLinea.indexOfFirst { it.nombre == origen.nombre }
            val indexDestino = estacionesMismaLinea.indexOfFirst { it.nombre == destino.nombre }

            if (indexOrigen != -1 && indexDestino != -1) {
                val subRuta = if (indexOrigen < indexDestino)
                    estacionesMismaLinea.subList(indexOrigen, indexDestino + 1)
                else
                    estacionesMismaLinea.subList(indexDestino, indexOrigen + 1).reversed()

                subRuta.forEachIndexed { index, estacion ->
                    val tiempo = if (index < subRuta.size - 1) (1..2).random() else 0
                    pasos.add(PasoEstaciones(estacion, tiempo))
                }
            }
        } else {
            // Paso 2: transbordo entre líneas (abajo)
            pasos.addAll(calcularRutaConTransbordo(origen, destino))
        }

        return pasos
    }

    private fun calcularRutaConTransbordo(origen: Estacion, destino: Estacion): List<Any> {
        val pasos = mutableListOf<Any>()

        // 1. Obtener todas las estaciones de cada línea
        val estacionesOrigen = estaciones.filter { it.linea.nombre == origen.linea.nombre }
        val estacionesDestino = estaciones.filter { it.linea.nombre == destino.linea.nombre }

        // 2. Buscar nombres de estaciones comunes entre ambas líneas (puntos de transbordo)
        val nombresComunes = estacionesOrigen.map { it.nombre }
            .intersect(estacionesDestino.map { it.nombre })

        // 3. Si no hay transbordo común, no se puede continuar
        if (nombresComunes.isEmpty()) {
            showToast("No se encontró una estación de transbordo entre las líneas.")
            return emptyList()
        }

        // 4. Inicializar variables para guardar el mejor resultado
        var mejorRuta: List<Any> = emptyList()
        var menorCantidadDePasos = Int.MAX_VALUE

        // 5. Probar cada transbordo y ver cuál da la ruta más corta
        for (nombreTransbordo in nombresComunes) {
            val indexOrigen = estacionesOrigen.indexOfFirst { it.nombre == origen.nombre }
            val indexTransO = estacionesOrigen.indexOfFirst { it.nombre == nombreTransbordo }

            val indexDestino = estacionesDestino.indexOfFirst { it.nombre == destino.nombre }
            val indexTransD = estacionesDestino.indexOfFirst { it.nombre == nombreTransbordo }

            val tramo1 = if (indexOrigen < indexTransO)
                estacionesOrigen.subList(indexOrigen, indexTransO + 1)
            else
                estacionesOrigen.subList(indexTransO, indexOrigen + 1).reversed()

            val tramo2 = if (indexTransD < indexDestino)
                estacionesDestino.subList(indexTransD + 1, indexDestino + 1)
            else
                estacionesDestino.subList(indexDestino, indexTransD).reversed()

            val totalEstaciones = tramo1.size + tramo2.size

            if (totalEstaciones < menorCantidadDePasos) {
                val ruta = mutableListOf<Any>()

                // Añadir primer tramo
                tramo1.forEachIndexed { i, est ->
                    val tiempo = if (i < tramo1.size - 1) (1..2).random() else 0
                    ruta.add(PasoEstaciones(est, tiempo))
                }

                // Añadir transbordo
                val estacionTransbordo = estacionesOrigen.find { it.nombre == nombreTransbordo }!!
                ruta.add(PasoTransbordo(estacionTransbordo, destino.linea, (2..3).random()))

                // Añadir segundo tramo
                tramo2.forEachIndexed { i, est ->
                    val tiempo = if (i < tramo2.size - 1) (1..2).random() else 0
                    ruta.add(PasoEstaciones(est, tiempo))
                }

                // Actualizar si esta ruta es mejor
                mejorRuta = ruta
                menorCantidadDePasos = totalEstaciones
            }
        }

        return mejorRuta
    }



    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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