package com.example.movicard

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.movicard.databinding.ActivityPurchaseSummaryBinding
import com.google.android.material.navigation.NavigationView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class PurchaseSummary : BaseActivity() {
    private lateinit var binding: ActivityPurchaseSummaryBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPurchaseSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar la Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)


        // Configurar el menú lateral
        drawerLayout = binding.drawerLayoutPurchaseSummary
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


        // Aplica fecha y hora actual al resumen de la compra
        aplicaHoraActual()

        // Obtengo los valores de PaymentDetails y los agrego en esta clase
        intent.extras?.apply {
            binding.nombre.setText(getString("nombre"))
            binding.apellido.setText(getString("apellido"))
            binding.correo.setText(getString("correo"))
            binding.telefono.setText(getString("telefono"))
            binding.direccion.setText(getString("direccion"))
            binding.localidad.setText(getString("localidad"))
        }

        binding.descargar.setOnClickListener {
            generarPDF(this)
        }
    }

    fun generarPDF(context: Context) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        // Obtener la imagen desde drawable
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.movicard_foto_comprimida)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, false)

        // Dibujar la imagen en la parte superior centrada
        val imageX = (pageInfo.pageWidth - scaledBitmap.width) / 2f  // Centrar horizontalmente
        val imageY = 20f  // Margen superior
        canvas.drawBitmap(scaledBitmap, imageX, imageY, paint)

        // Ajustar la posición del texto después de la imagen
        var yPos = imageY + scaledBitmap.height + 20f  // Espacio después del logo

        // Establecer el título principal
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        paint.color = Color.BLACK
        canvas.drawText("Factura de Compra", 200f, yPos, paint)
        yPos += 30f  // Espacio entre líneas

        // Dibujar línea separadora
        paint.strokeWidth = 2f
        canvas.drawLine(50f, yPos, 545f, yPos, paint)
        yPos += 30f

        // Información general
        paint.textSize = 14f
        paint.typeface = Typeface.DEFAULT
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        val currentDate = sdf.format(Date())
        canvas.drawText("Fecha: $currentDate", 50f, yPos, paint)
        yPos += 30f

        val nombre = context.getString(R.string.noms) + ": " + binding.nombre.toString()
        canvas.drawText(nombre, 50f, yPos, paint)
        yPos += 30f

        val apellido = context.getString(R.string.cognoms) + ": " + binding.apellido.toString()
        canvas.drawText(apellido, 50f, yPos, paint)
        yPos += 30f

        val correo = context.getString(R.string.correu) + ": " + binding.correo.toString()
        canvas.drawText(correo, 50f, yPos, paint)
        yPos += 30f

        val telefono = context.getString(R.string.tel_fon) + ": " + binding.telefono.toString()
        canvas.drawText(telefono, 50f, yPos, paint)
        yPos += 30f

        val direccion = context.getString(R.string.direcci) + ": " + binding.direccion.toString()
        canvas.drawText(direccion, 50f, yPos, paint)
        yPos += 30f

        val localidad = context.getString(R.string.localitat) + ": " + binding.localidad.toString()
        canvas.drawText(localidad, 50f, yPos, paint)
        yPos += 30f

        // Dibujar otra línea separadora
        canvas.drawLine(50f, yPos, 545f, yPos, paint)
        yPos += 30f

        // Información de compra
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        canvas.drawText(getString(R.string.detalle_del_pedido), 50f, yPos, paint)
        yPos += 30f

        paint.textSize = 14f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(getString(R.string.producto_suscripci_n_premium), 50f, yPos, paint)
        yPos += 30f
        canvas.drawText(getString(R.string.cantidad_1), 50f, yPos, paint)
        yPos += 30f
        canvas.drawText(getString(R.string.precio_unitario_10), 50f, yPos, paint)
        yPos += 30f
        canvas.drawText(getString(R.string.total_10), 50f, yPos, paint)
        yPos += 30f

        // Línea final
        canvas.drawLine(50f, yPos, 545f, yPos, paint)
        yPos += 30f

        // Información adicional
        paint.textSize = 12f
        paint.color = Color.GRAY
        canvas.drawText("Gracias por su compra. Para más información, visite nuestra página web: " /*Aquí quiero poner "Movicard"*/, 50f, yPos, paint)

        document.finishPage(page)

        // Guardar el PDF en la carpeta de descargas
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(directory, "Factura_Compra.pdf")
        try {
            val fos = FileOutputStream(file)
            document.writeTo(fos)
            document.close()
            Toast.makeText(context, "PDF guardado en Descargas", Toast.LENGTH_SHORT).show()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
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

    private fun aplicaHoraActual(){
        // Obtener la fecha y hora actual
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val fechaHoraActual = sdf.format(Date())

        // Asignarla al TextView
        binding.fechayhora.text = fechaHoraActual
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