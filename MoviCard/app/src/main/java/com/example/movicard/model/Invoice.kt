package com.example.movicard.model

data class Invoice(
    val id: Int,
    val name: String,
    val date: String,
    val amount: Double,
    val url: String,
    val filePath: String?,
    val paymentIntentId: String
)
