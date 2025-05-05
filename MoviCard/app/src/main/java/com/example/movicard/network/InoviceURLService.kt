package com.example.movicard.network

import com.example.movicard.data.ReceiptResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface InoviceURLService {
    @GET("get-receipt-url/{paymentIntentId}")
    fun getReceiptUrl(@Path("paymentIntentId") paymentIntentId: String): Call<ReceiptResponse>
}