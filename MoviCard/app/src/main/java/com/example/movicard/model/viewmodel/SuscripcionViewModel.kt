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

    // Crea el plan de suscripción en GRATUITO
    fun creaSuscripcion() {
        val clienteSession = sessionManager.getCliente()

        if (clienteSession != null) {
            viewModelScope.launch {
                try {
                    // Intentamos obtener la suscripción actual
                    val suscripcionExistente = api.getSuscripcion(clienteSession.id)

                    // Si ya existe, no la creamos de nuevo
                    Log.d(
                        "SUSCRIPCION",
                        "Ya existe una suscripción: ${suscripcionExistente.suscripcion}"
                    )
                    _suscripcion.value = suscripcionExistente

                } catch (e: Exception) {
                    Log.w(
                        "SUSCRIPCION",
                        "No se encontró suscripción, se intentará crear una nueva..."
                    )

                    try {
                        // Si no existe, la creamos
                        val nuevaSuscripcion = api.createSuscripcion(clienteSession.id)
                        _suscripcion.value = nuevaSuscripcion
                        Log.d("SUSCRIPCION", "Suscripción creada: ${nuevaSuscripcion.suscripcion}")
                    } catch (creationError: Exception) {
                        creationError.printStackTrace()
                        Log.e("SUSCRIPCION", "Error al crear suscripción: ${creationError.message}")
                    }
                }
            }
        } else {
            Log.w("SUSCRIPCION", "Cliente no encontrado en sesión")
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