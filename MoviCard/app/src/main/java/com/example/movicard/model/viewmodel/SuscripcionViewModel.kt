package com.example.movicard.model.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movicard.helper.SessionManager
import com.example.movicard.model.Suscripcion
import com.example.movicard.network.ClienteApiService
import kotlinx.coroutines.launch

class SuscripcionViewModel(
    private val api: ClienteApiService,         // Tu interfaz de Retrofit
    private val sessionManager: SessionManager,  // Para obtener el ID del cliente actual
) : ViewModel() {

    private val _suscripcion = MutableLiveData<Suscripcion>()
    val suscripcion: LiveData<Suscripcion> get() = _suscripcion

    // Obtener la suscripción actual del cliente desde la API
    fun cargarSuscripcion() {
        val clienteSession = sessionManager.getCliente()

        if (clienteSession != null) {
            viewModelScope.launch {
                try {
                    val suscripcionApi = api.getSuscripcion(clienteSession.id)
                    _suscripcion.value = suscripcionApi
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Cambiar el plan del cliente a PREMIUM
    fun actualizarASuscripcionPremium() {
        val clienteSession = sessionManager.getCliente()

        if (clienteSession != null) {
            viewModelScope.launch {
                try {
                    val nuevaSuscripcion = api.actualizarSuscripcionPremium(clienteSession.id)
                    _suscripcion.value = nuevaSuscripcion
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Cambiar el plan del cliente a PREMIUM
    fun actualizarASuscripcionGratuita() {
        val clienteSession = sessionManager.getCliente()

        if (clienteSession != null) {
            viewModelScope.launch {
                try {
                    val nuevaSuscripcion = api.actualizarSuscripcionGratuita(clienteSession.id)
                    _suscripcion.value = nuevaSuscripcion
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun verificaSuscripcionGratuita(onResult: (Boolean) -> Unit) {
        val clienteSession = sessionManager.getCliente()

        if (clienteSession != null) {
            viewModelScope.launch {
                val suscripcion = api.getSuscripcion(clienteSession.id)
                try {
                    Log.w("SUSCRIPCIÓN", "La suscripción actual es: ${suscripcion.suscripcion}")
                    onResult(suscripcion.suscripcion == "GRATUITA")
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.w("SUSCRIPCIÓN", "La suscripción es premium: ${suscripcion.suscripcion}")
                    onResult(false)
                }
            }
        } else {
            onResult(false)
        }
    }
}