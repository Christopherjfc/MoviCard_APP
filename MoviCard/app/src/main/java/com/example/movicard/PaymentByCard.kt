package com.example.movicard

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.movicard.helper.InvoiceDatabaseHelper
import com.google.gson.Gson
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class PaymentByCard : AppCompatActivity() {

    private lateinit var paymentSheet: PaymentSheet
    private var amount: Int = 0 // Monto en centavos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_by_card)

        val titulo = intent.getStringExtra("titulo") ?: ""
        val premium = intent.getStringExtra("premium") ?: ""

        // Definir el monto según el título
        amount = when {
            titulo == "TENMOVI" -> 1385 // 12.55€ en centavos
            titulo == "MOVIMES" -> 2420 // 22.00€ en centavos
            titulo == "TRIMOVI" -> 4840 // 48.40€ en centavos
            premium == "SUSCRIPCIÓN PREMIUM" -> 1000 // 10.00 € en centavos
            else -> 0
        }

        if (amount == 0) {
            Toast.makeText(this, "Error: título no válido", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Agregar la clave pública de Stripe
        PaymentConfiguration.init(this, "pk_test_51R9oJRD304ojpXkK2ZVUTGZhusb7Yj3BFLFCalKDMQV8vG644nX8i7P6gIefcWQj54lxFkdSEXDl1bR8bUckgJ1h00t2kXkjIk")

        paymentSheet = PaymentSheet(this, ::onPaymentResult)
        if (isNetworkAvailable()) {
            createPaymentIntent()
        } else {
            Toast.makeText(this, "No hay conexión a Internet", Toast.LENGTH_SHORT).show()
        }
    }

    // Verificar si hay conexión a Internet
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

    private fun createPaymentIntent() {
        val client = OkHttpClient()
        val requestBody = JSONObject().apply {
            put("amount", amount)
            put("currency", "eur")
        }.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("http://192.168.56.3:3000/create-payment-intent") // Backend local para pruebas
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@PaymentByCard, "Error al conectarse al servidor", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful) {
                    try {
                        val json = JSONObject(responseBody)
                        if (json.has("clientSecret")) {
                            val clientSecret = json.getString("clientSecret")
                            println(clientSecret)
                            val paymentIntentId = clientSecret.substringBefore("_secret") // Extraer ID

                            // Guardar la ID para usarla después en onPaymentResult
                            runOnUiThread {
                                paymentSheet.presentWithPaymentIntent(clientSecret)
                            }

                            // Pasar la ID a la siguiente actividad
                            intent.putExtra("paymentIntentId", paymentIntentId)
                        }
                    } catch (e: JSONException) {
                        println("Error al procesar la respuesta JSON: ${e.message}")
                    }
                } else {
                    println("Error en la respuesta del servidor: ${response.message}")
                }
            }
        })
    }

    private fun onPaymentResult(paymentResult: PaymentSheetResult) {
        when (paymentResult) {
            is PaymentSheetResult.Completed -> {
                Toast.makeText(this, "Pago exitoso", Toast.LENGTH_SHORT).show()

                val titulo = intent.getStringExtra("titulo") ?: ""
                val premium = intent.getStringExtra("premium") ?: ""
                val precio = intent.getStringExtra("precio") ?: "0"
                val nombre = intent.getStringExtra("nombre")
                val apellido = intent.getStringExtra("apellido")
                val correo = intent.getStringExtra("correo")
                val telefono = intent.getStringExtra("telefono")
                val direccion = intent.getStringExtra("direccion")
                val localidad = intent.getStringExtra("localidad")

                val paymentIntentId = intent.getStringExtra("paymentIntentId") ?: ""

                println(premium)

                if (paymentIntentId.isEmpty()) {
                    Toast.makeText(this, "Error: No se encontró paymentIntentId", Toast.LENGTH_SHORT).show()
                    return
                }

                // 🔹 Llamar a la función para obtener la URL de la factura
                obtenerUrlFactura(paymentIntentId) { urlFactura ->
                    runOnUiThread {
                        val dbHelper = InvoiceDatabaseHelper(this)
                        val success = when {
                            titulo.isNotEmpty() -> dbHelper.insertInvoice(titulo, precio, paymentIntentId, urlFactura)
                            premium.isNotEmpty() -> dbHelper.insertInvoice(premium, precio, paymentIntentId, urlFactura)
                            else -> false
                        }

                        if (success) {
                            Toast.makeText(this, "Factura guardada correctamente", Toast.LENGTH_SHORT).show()

                            // Guardo las tarjetas para luego contarlas y llenar el gráfico Pie.
                            // Así podré saber cuantas tarjetas se han ido comprando al paso del tiempo
                            val tarjetas = TarjetaStorage.cargarTarjetas(this)

                            tarjetas.add(Tarjeta(if (titulo.isNotEmpty()) titulo else premium))

                            TarjetaStorage.guardarTarjetas(this, tarjetas)


                            /*
                            * LLENO LA LISTA DE TARJETAS PARA LLENAR LA GRÁFICA REDONDA
                            */

                            // Por cada tarjeta que se vaya comprando voy sumando monto al total de gastos
                            val prefs = getSharedPreferences("TarjetasPrefs", Context.MODE_PRIVATE)
                            val totalAnterior = prefs.getInt("gasto_total", 0)
                            prefs.edit().putInt("gasto_total", totalAnterior + amount).apply()


                            /*
                            * LLENO LA GRÁFICA DE BARRAS
                            */

                            // ➕ Guardar gasto por fecha
                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                            val fechaHoy = sdf.format(java.util.Date()) // Ej: 2025-04-04
                            val gastoPorDiaJson = prefs.getString("gastos_por_dia", "{}")
                            val gastoMap: MutableMap<String, Int> = Gson().fromJson(
                                gastoPorDiaJson,
                                object : com.google.gson.reflect.TypeToken<MutableMap<String, Int>>() {}.type
                            ) ?: mutableMapOf()

                            val gastoAnterior = gastoMap[fechaHoy] ?: 0
                            gastoMap[fechaHoy] = gastoAnterior + amount

                            // Guardar actualizado
                            prefs.edit().putString("gastos_por_dia", Gson().toJson(gastoMap)).apply()

                        } else {
                            Toast.makeText(this, "Error al guardar la factura", Toast.LENGTH_SHORT).show()
                        }

                        val intent = Intent(this, AnimationRegisterCard::class.java).apply {
                            putExtra("titulo", titulo)
                            putExtra("premium", premium)
                            putExtra("precio", precio)
                            putExtra("nombre", nombre)
                            putExtra("apellido", apellido)
                            putExtra("correo", correo)
                            putExtra("telefono", telefono)
                            putExtra("direccion", direccion)
                            putExtra("localidad", localidad)
                            putExtra("paymentIntentId", paymentIntentId)
                        }
                        startActivity(intent)
                    }
                }
            }

            is PaymentSheetResult.Canceled -> {
                Toast.makeText(this, "Pago cancelado", Toast.LENGTH_SHORT).show()
                finish()
            }

            is PaymentSheetResult.Failed -> {
                Toast.makeText(this, "Error en el pago", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun obtenerUrlFactura(paymentIntentId: String, callback: (String) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://192.168.56.3:3000/get-receipt-url/$paymentIntentId") // 🔹 Reemplaza con la IP de tu backend si pruebas en un móvil
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback("Sin URL") // 🔹 En caso de error, devuelve un valor por defecto
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    try {
                        val json = JSONObject(responseBody)
                        val receiptUrl = json.optString("receiptUrl", "Sin URL") // 🔹 Obtiene la URL del JSON
                        callback(receiptUrl)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        callback("Sin URL")
                    }
                } else {
                    callback("Sin URL")
                }
            }
        })
    }
}
