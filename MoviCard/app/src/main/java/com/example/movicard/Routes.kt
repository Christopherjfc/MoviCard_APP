package com.example.movicard

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movicard.data.RutaAdapter
import com.example.movicard.databinding.ActivityRoutesBinding
import com.example.movicard.helper.SessionManager
import com.example.movicard.model.Estacion
import com.example.movicard.model.Linea
import com.example.movicard.model.PasoEstaciones
import com.example.movicard.model.PasoTransbordo
import com.example.movicard.model.Trayecto
import com.example.movicard.model.viewmodel.TarjetaViewModel
import com.example.movicard.model.viewmodel.UsuarioViewModelFactory
import com.example.movicard.network.RetrofitInstanceAPI
import com.google.android.material.navigation.NavigationView
import java.util.LinkedList
import java.util.Queue

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
            val rutaCalculada = calcularRutaMultipleTransbordos(origen, destino)
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

    private fun calcularRutaMultipleTransbordos(origenNombre: String, destinoNombre: String): List<Any> {
        val origenes = estaciones.filter { it.nombre.equals(origenNombre, ignoreCase = true) }
        val destinos = estaciones.filter { it.nombre.equals(destinoNombre, ignoreCase = true) }

        if (origenes.isEmpty() || destinos.isEmpty()) {
            showToast("Estación no encontrada.")
            return emptyList()
        }

        val queue: Queue<Trayecto> = LinkedList()
        for (origen in origenes) {
            queue.add(Trayecto(origen, origen.linea, mutableListOf(origen), mutableListOf()))
        }

        val visitados = mutableSetOf<Pair<String, String>>()

        while (queue.isNotEmpty()) {
            val actual = queue.poll()

            if (destinos.any { it.nombre == actual.estacion.nombre }) {
                return construirPasosDesdeTrayecto(actual)
            }

            if (!visitados.add(Pair(actual.estacion.nombre, actual.linea.nombre))) continue

            val vecinas = obtenerEstacionesVecinas(actual.estacion, actual.linea)
            for (vecina in vecinas) {
                val nuevaRuta = actual.ruta.toMutableList().apply { add(vecina) }
                queue.add(Trayecto(vecina, actual.linea, nuevaRuta, actual.transbordos.toMutableList()))
            }

            val posiblesCambios = estaciones.filter {
                it.nombre == actual.estacion.nombre && it.linea.nombre != actual.linea.nombre
            }

            for (cambio in posiblesCambios) {
                val nuevaRuta = actual.ruta.toMutableList()
                val nuevosTransbordos = actual.transbordos.toMutableList()
                nuevosTransbordos.add(PasoTransbordo(actual.estacion, cambio.linea, (2..3).random()))
                queue.add(Trayecto(cambio, cambio.linea, nuevaRuta, nuevosTransbordos))
            }
        }

        showToast("No se encontró una ruta posible.")
        return emptyList()
    }

    private fun obtenerEstacionesVecinas(estacion: Estacion, linea: Linea): List<Estacion> {
        val mismaLinea = estaciones.filter { it.linea == linea }
        val index = mismaLinea.indexOfFirst { it.nombre == estacion.nombre }
        val vecinas = mutableListOf<Estacion>()
        if (index > 0) vecinas.add(mismaLinea[index - 1])
        if (index < mismaLinea.size - 1) vecinas.add(mismaLinea[index + 1])
        return vecinas
    }

    private fun construirPasosDesdeTrayecto(trayecto: Trayecto): List<Any> {
        val resultado = mutableListOf<Any>()
        val estacionesRuta = trayecto.ruta
        val transbordos = trayecto.transbordos

        for (i in estacionesRuta.indices) {
            resultado.add(
                PasoEstaciones(estacionesRuta[i], if (i < estacionesRuta.size - 1) (1..2).random() else 0)
            )
            val transb = transbordos.find { it.estacionTransbordo.nombre == estacionesRuta[i].nombre }
            if (transb != null) resultado.add(transb)
        }

        return resultado
    }



    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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