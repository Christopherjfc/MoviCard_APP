package com.example.movicard.model.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movicard.helper.SessionManager
import com.example.movicard.model.TarjetaMoviCard
import com.example.movicard.network.ClienteApiService
import kotlinx.coroutines.launch

class TarjetaViewModel(
    private val api: ClienteApiService,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _tarjeta = MutableLiveData<TarjetaMoviCard?>()
    val tarjeta: LiveData<TarjetaMoviCard?> get() = _tarjeta

    fun cargarTarjeta() {
        val cliente = sessionManager.getCliente()
        if (cliente != null) {
            viewModelScope.launch {
                try {
                    Log.d("TarjetaViewModel", "Cargando tarjeta del cliente con ID: ${cliente.id}")
                    val tarjetaApi = api.getTarjetaByClienteId(cliente.id)
                    _tarjeta.value = tarjetaApi
                    Log.d("TarjetaViewModel", "Tarjeta cargada: $tarjetaApi")
                } catch (e: Exception) {
                    Log.e("TarjetaViewModel", "Error al cargar tarjeta: ${e.message}")
                    _tarjeta.value = null
                }
            }
        } else {
            Log.e("TarjetaViewModel", "Cliente no encontrado en sesión al cargar tarjeta")
        }
    }

    fun crearTarjeta(idSuscripcion: Int, idTicket: Int? = null) {
        val clienteSession = sessionManager.getCliente()
        if (clienteSession != null) {
            viewModelScope.launch {
                try {
                    // Intentamos obtener la suscripción actual
                    val tarjetaExistente = api.getTarjetaByClienteId(clienteSession.id)


                    // Si ya existe, no la creamos de nuevo.
                    Log.d("TarjetaMovicard", "Ya existe una Tarjeta!")
                    _tarjeta.value = tarjetaExistente

                    // Si la tarjeta no existe, entra al catch y se crea una
                } catch (e: Exception) {
                    try {
                        api.crearTarjeta(
                            idCliente = clienteSession.id,
                            idSuscripcion = idSuscripcion,
                            idTicket = idTicket
                        )
                        cargarTarjeta()
                        Log.d("TarjetaViewModel", "Tarjeta creada exitosamente")
                    } catch (creationError: Exception) {
                        Log.e("TarjetaViewModel", "Error al crear tarjeta: ${creationError.message}")
                    }
                }
            }
        } else {
            Log.e("TarjetaViewModel", "Cliente no encontrado en sesión al crear tarjeta")
        }
    }

    fun cambiarEstadoTarjeta(nuevoEstado: String) {
        val cliente = sessionManager.getCliente()
        if (cliente != null) {
            viewModelScope.launch {
                try {
                    Log.d(
                        "TarjetaViewModel",
                        "Cambiando estado de tarjeta a $nuevoEstado para cliente ${cliente.id}"
                    )
                    api.actualizarEstadoTarjeta(cliente.id, nuevoEstado)
                    Log.d("TarjetaViewModel", "Estado de tarjeta actualizado a $nuevoEstado")
                } catch (e: Exception) {
                    Log.e("TarjetaViewModel", "Error al cambiar estado de tarjeta: ${e.message}")
                }
            }
        } else {
            Log.e("TarjetaViewModel", "Cliente no encontrado en sesión al cambiar estado")
        }
    }

    fun cambiarEstadoActivacionTarjeta(nuevoEstadoActivacion: String) {
        val cliente = sessionManager.getCliente()
        if (cliente != null) {
            viewModelScope.launch {
                try {
                    Log.d(
                        "TarjetaViewModel",
                        "Cambiando estado de activación de la tarjeta a $nuevoEstadoActivacion para cliente ${cliente.id}"
                    )
                    api.actualizarEstadoActivacionTarjeta(cliente.id, nuevoEstadoActivacion)
                    Log.d("TarjetaViewModel", "Estado de activación de la tarjeta actualizado a $nuevoEstadoActivacion")
                } catch (e: Exception) {
                    Log.e("TarjetaViewModel", "Error al activar la tarjeta: ${e.message}")
                }
            }
        } else {
            Log.e("TarjetaViewModel", "Cliente no encontrado en sesión al cambiar estado de activación")
        }
    }

    fun existeTarjeta(onResult: (Boolean) -> Unit) {
        val cliente = sessionManager.getCliente()
        if (cliente != null) {
            viewModelScope.launch {
                try {
                    val tarjeta = api.getTarjetaByClienteId(cliente.id)
                    Log.d("TarjetaViewModel", "Existe tarjeta: $tarjeta")
                    onResult(true)
                } catch (e: Exception) {
                    Log.e(
                        "TarjetaViewModel",
                        "Error al verificar existencia de tarjeta: ${e.message}"
                    )
                    onResult(false)
                }
            }
        } else {
            Log.e(
                "TarjetaViewModel",
                "Cliente no encontrado en sesión al verificar existencia de tarjeta"
            )
            onResult(false)
        }
    }

    fun actualizarIdTicket(ticketId: Int) {
        val cliente = sessionManager.getCliente()
        if (cliente != null) {
            viewModelScope.launch {
                try {
                    api.actualizarIdTicket(cliente.id, ticketId)
                    cargarTarjeta()
                    Log.d("TarjetaViewModel", "ID del ticket actualizado a $ticketId")
                } catch (e: Exception) {
                    Log.e("TarjetaViewModel", "Error al actualizar id_ticket: ${e.message}")
                }
            }
        }
    }

}