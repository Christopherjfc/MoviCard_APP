package com.example.movicard.API

import com.example.movicard.data.ReceiptResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface APIService {
    @GET("get-receipt-url/{paymentIntentId}")
    fun getReceiptUrl(@Path("paymentIntentId") paymentIntentId: String): Call<ReceiptResponse>
}