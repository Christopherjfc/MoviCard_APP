package com.example.movicard.network

import com.example.movicard.model.ReceiptResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface InoviceURLService {
    //endpoint para obtener la url de la factura de Stripe
    //la uso para visualizar y descargar la factura
    @GET("get-receipt-url/{paymentIntentId}")
    fun getReceiptUrl(@Path("paymentIntentId") paymentIntentId: String): Call<ReceiptResponse>
}