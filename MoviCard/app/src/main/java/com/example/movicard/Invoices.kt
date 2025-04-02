package com.example.movicard

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movicard.DbHelper.InvoiceDatabaseHelper
import com.example.movicard.data.Invoice
import com.example.movicard.data.InvoiceAdapter
import com.example.movicard.databinding.ActivityGraficasBinding
import com.example.movicard.databinding.ActivityInvoicesBinding
import com.google.android.material.navigation.NavigationView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Invoices : BaseActivity(), InvoiceAdapter.InvoiceClickListener {
    private lateinit var binding: ActivityInvoicesBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var recyclerView: RecyclerView
    private lateinit var invoiceAdapter: InvoiceAdapter
    private lateinit var databaseHelper: InvoiceDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvoicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        // RecyclerView

        binding.recyclerViewInvoices.layoutManager = LinearLayoutManager(this)

        databaseHelper = InvoiceDatabaseHelper(this)
        loadInvoices();
    }

    private fun loadInvoices() {
        val invoices = databaseHelper.getAllInvoices()
        invoiceAdapter = InvoiceAdapter(invoices, this)
        recyclerView.adapter = invoiceAdapter
    }

    override fun onViewInvoice(invoice: Invoice) {
        Toast.makeText(this, "Abriendo factura: ${invoice.name}", Toast.LENGTH_SHORT).show()
        // Aquí puedes abrir el PDF o la URL de la factura
    }

    override fun onDownloadInvoice(invoice: Invoice) {
        val fileName = "Factura_${invoice.name}.txt"
        val downloadsDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        try {
            FileOutputStream(file).use { fos ->
                fos.write("Factura: ${invoice.name}\nFecha: ${invoice.date}\nMonto: ${invoice.amount}".toByteArray())
            }
            Toast.makeText(this, "Factura guardada en: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            Toast.makeText(this, "Error al guardar la factura", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
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