package com.example.movicard.model.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movicard.helper.SessionManager
import com.example.movicard.model.Cliente
import com.example.movicard.network.ClienteApiService
import kotlinx.coroutines.launch

// ViewModel que mantiene y gestiona los datos del cliente para la UI
class ClienteViewModel(
    private val api: ClienteApiService,           // Interfaz de Retrofit para obtener datos del backend
    private val sessionManager: SessionManager    // Maneja la sesión del usuario actual
) : ViewModel() {

    // LiveData privado para contener el cliente, sólo modificable desde dentro del ViewModel
    private val _cliente = MutableLiveData<Cliente>()

    // LiveData público para observar desde la UI sin poder modificar
    val cliente: LiveData<Cliente> get() = _cliente

    // Función que carga los datos del cliente desde la API
    fun cargarCliente() {
        // Obtenemos el cliente almacenado en sesión (SharedPreferences)
        val clienteSession = sessionManager.getCliente()

        // Si existe cliente en sesión, lanzamos una corrutina para obtener sus datos desde el backend
        if (clienteSession != null) {
            viewModelScope.launch {
                try {
                    // Llamada a la API usando la ID del cliente
                    val clienteApi = api.getClienteById(clienteSession.id)

                    // Actualizamos el LiveData con la respuesta
                    _cliente.value = clienteApi

                } catch (e: Exception) {
                    // Si hay error, lo mostramos en consola por ahora
                    e.printStackTrace()
                    // Aquí podrías tener otro LiveData para errores si quieres mostrar mensajes al usuario
                }
            }
        }
    }
}