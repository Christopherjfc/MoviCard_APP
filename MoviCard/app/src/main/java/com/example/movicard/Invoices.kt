package com.example.movicard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movicard.helper.InvoiceDatabaseHelper
import com.example.movicard.model.Invoice
import com.example.movicard.data.InvoiceAdapter
import com.example.movicard.databinding.ActivityInvoicesBinding
import com.google.android.material.navigation.NavigationView
import android.net.Uri
import com.example.movicard.model.ReceiptResponse
import retrofit2.Call
import retrofit2.Response

// Descargar pdf con WebView a partir de una URL
import android.print.PrintManager
import android.print.PrintAttributes
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.movicard.helper.SessionManager
import com.example.movicard.model.viewmodel.ClienteViewModel
import com.example.movicard.model.viewmodel.TarjetaViewModel
import com.example.movicard.model.viewmodel.UsuarioViewModelFactory
import com.example.movicard.network.RetrofitInstanceStripeAPI
import com.example.movicard.network.RetrofitInstanceAPI


class Invoices : BaseActivity(), InvoiceAdapter.InvoiceClickListener {
    private lateinit var binding: ActivityInvoicesBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var invoiceAdapter: InvoiceAdapter
    private lateinit var databaseHelper: InvoiceDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvoicesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // RecyclerView
        binding.recyclerViewInvoices.layoutManager = LinearLayoutManager(this)

        // Configurar la Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)


        // Configurar el menú lateral
        drawerLayout = binding.drawerLayoutInvoices
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

        databaseHelper = InvoiceDatabaseHelper(this)
        loadInvoices();

        // Méthod test para borrar todas las facturas
//         databaseHelper.deleteAllInvoices()
    }

    private fun loadInvoices() {
        val invoices = databaseHelper.getAllInvoices()
        invoiceAdapter = InvoiceAdapter(invoices, this)
        binding.recyclerViewInvoices.adapter = invoiceAdapter
    }

    override fun onViewInvoice(invoice: Invoice) {
        val paymentIntentId = invoice.paymentIntentId // Debes almacenar el PaymentIntent ID en tu BD local
        println(paymentIntentId)
        val call = RetrofitInstanceStripeAPI.instance.getReceiptUrl(paymentIntentId)

        call.enqueue(object : retrofit2.Callback<ReceiptResponse> {
            override fun onResponse(call: Call<ReceiptResponse>, response: Response<ReceiptResponse>) {
                if (response.isSuccessful) {
                    val receiptUrl = response.body()?.receiptUrl
                    if (!receiptUrl.isNullOrEmpty()) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(receiptUrl))
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@Invoices, "No se encontró la factura", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@Invoices, "Error al obtener la factura", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ReceiptResponse>, t: Throwable) {
                Toast.makeText(this@Invoices, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun onDownloadInvoice(invoice: Invoice) {
        val invoiceUrl = invoice.url  // Asegúrate de que este es el enlace de la factura
        System.out.println(invoiceUrl)
        saveWebPageAsPdf(invoiceUrl)
    }

    private fun saveWebPageAsPdf(invoiceUrl: String) {
        val webView = WebView(this)
        webView.settings.javaScriptEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
                val printAdapter = webView.createPrintDocumentAdapter("Factura")
                val jobName = "Factura_${System.currentTimeMillis()}"

                val printJob = printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())

                if (printJob.isCompleted) {
                    Toast.makeText(this@Invoices, "Factura guardada como PDF", Toast.LENGTH_SHORT).show()
                } else if (printJob.isFailed) {
                    Toast.makeText(this@Invoices, "Error al guardar la factura", Toast.LENGTH_SHORT).show()
                }
            }
        }

        webView.loadUrl(invoiceUrl)
    }

    private fun isTargetActivated() : Boolean{
        var estaActivada : Boolean = false
        val sessionManager = SessionManager(this)
        // creo el ViewModel usando el Factory personalizado
        val viewModelFactory = UsuarioViewModelFactory(RetrofitInstanceAPI.api, sessionManager)
        val viewModelTarjeta = ViewModelProvider(this, viewModelFactory).get(TarjetaViewModel::class.java)

        viewModelTarjeta.cargarTarjeta()

        viewModelTarjeta.tarjeta.observe(this) { tarjeta ->
            estaActivada = tarjeta?.estadotarjeta != "DESACTIVADA"
        }
        return estaActivada
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
                startActivity(Intent(this, CardSettings::class.java))
                return true
            }
        }
        return false
    }
}