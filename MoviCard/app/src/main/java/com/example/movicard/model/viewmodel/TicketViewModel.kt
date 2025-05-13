package com.example.movicard.model.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movicard.helper.SessionManager
import com.example.movicard.model.Ticket
import com.example.movicard.network.ClienteApiService
import kotlinx.coroutines.launch

class TicketViewModel(
    private val api: ClienteApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _ticket = MutableLiveData<Ticket?>()
    val ticket: LiveData<Ticket?> get() = _ticket

    fun cargarTicket() {
        val cliente = sessionManager.getCliente()
        if (cliente != null) {
            viewModelScope.launch {
                try {
                    val ticket = api.getTicketByClienteId(cliente.id)
                    _ticket.value = ticket
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun crearTicket(tipo: String) {
        val cliente = sessionManager.getCliente()
        if (cliente != null) {
            viewModelScope.launch {
                try {
                    api.crearTicket(tipo = tipo, idCliente = cliente.id)
                    cargarTicket()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun actualizarTicket(tipo: String) {
        val cliente = sessionManager.getCliente()
        if (cliente != null) {
            viewModelScope.launch {
                try {
                    api.updateTicket(cliente.id, tipo)
                    cargarTicket()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun existeTicket(onResult: (Boolean) -> Unit) {
        val cliente = sessionManager.getCliente()
        if (cliente != null) {
            viewModelScope.launch {
                try {
                    api.getTicketByClienteId(cliente.id)
                    println("YA existe un ticket con esta ID")
                    onResult(true)
                } catch (e: Exception) {
                    e.printStackTrace()
                    println("No existe un ticket con esta ID")
                    onResult(false)
                }
            }
        } else {
            onResult(false)
        }
    }
}