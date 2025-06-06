package com.example.movicard.model.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.movicard.helper.SessionManager
import com.example.movicard.network.ClienteApiService

class UsuarioViewModelFactory(
    private val api: ClienteApiService,          // Retrofit API service
    private val sessionManager: SessionManager   // Sesión del usuario
) : ViewModelProvider.Factory {

    // Método que crea una instancia del ViewModel y le pasa los parámetros necesarios
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Verificamos que el ViewModel solicitado sea del tipo correcto
        return when {

            // CLIENTE
            modelClass.isAssignableFrom(ClienteViewModel::class.java) -> {
                ClienteViewModel(api, sessionManager) as T
            }

            // SUSCRIPCIÓN
            modelClass.isAssignableFrom(SuscripcionViewModel::class.java) -> {
                SuscripcionViewModel(api, sessionManager) as T
            }

            // TICKET
            modelClass.isAssignableFrom(TicketViewModel::class.java) -> {
                TicketViewModel(api, sessionManager) as T
            }

            // TARJETA MOVICARD
            modelClass.isAssignableFrom(TarjetaViewModel::class.java) -> {
                TarjetaViewModel(api, sessionManager) as T
            }
            // Agregá más ViewModels aquí si es necesario
            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}