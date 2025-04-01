package com.example.movicard

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        // Definir el monto según el título
        amount = when (titulo) {
            "MOVI_10" -> 1385 // 12.55€ en centavos
            "MOVI_MES" -> 2420 // 22€ en centavos
            "MOVI_TRIMESTRAL" -> 4840 // 44€ en centavos
            else -> 0
        }

        if (amount == 0) {
            Toast.makeText(this, "Error: título no válido", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Agregar la clave pública de Stripe
        PaymentConfiguration.init(this, "pk_test_51R532HEdEU4VdYmpfHoGpEgOUZmmJgOs0UuWdWhVq3MQmjvRdH39fuxQK5lgU6gSXSwLqlDOx1MhRdcz0FIQcdOH00ywdNCzhM")

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
            .url("http://192.168.154.3:3000/create-payment-intent") // Backend local para pruebas
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
                val responseBody = response.body?.string() // Obtén el cuerpo de la respuesta como String
                println("Respuesta del backend: $responseBody") // Muestra la respuesta completa en el log
                println("Código de respuesta: ${response.code}") // Muestra el código de respuesta

                if (response.isSuccessful) {
                    // Si la respuesta es válida, intenta obtener el clientSecret
                    try {
                        val json = JSONObject(responseBody)
                        if (json.has("clientSecret")) {
                            val clientSecret = json.getString("clientSecret")
                            runOnUiThread {
                                paymentSheet.presentWithPaymentIntent(clientSecret)
                            }
                        } else {
                            println("No se encontró el campo clientSecret en la respuesta")
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
                val precio = intent.getStringExtra("precio") ?: ""
                val bundle = intent.extras  // Recuperamos el bundle con los datos

                val intent = Intent(this, AnimationRegisterCard::class.java)
                intent.putExtra("titulo", titulo)
                intent.putExtra("precio", precio)
                intent.putExtra("origen", "PrincingCards")

                // Si hay datos en el bundle, los pasamos
                if (bundle != null) {
                    intent.putExtras(bundle)
                }
                startActivity(intent)
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

}
