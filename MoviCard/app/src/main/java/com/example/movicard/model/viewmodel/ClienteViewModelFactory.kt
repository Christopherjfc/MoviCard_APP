package com.example.movicard.model.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.movicard.helper.SessionManager
import com.example.movicard.network.ClienteApiService

class ClienteViewModelFactory(
    private val api: ClienteApiService,          // Retrofit API service
    private val sessionManager: SessionManager   // Sesión del usuario
) : ViewModelProvider.Factory {

    // Método que crea una instancia del ViewModel y le pasa los parámetros necesarios
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Verificamos que el ViewModel solicitado sea del tipo correcto
        if (modelClass.isAssignableFrom(ClienteViewModel::class.java)) {
            return ClienteViewModel(api, sessionManager) as T
        }
        // Si no, lanzamos un error
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}