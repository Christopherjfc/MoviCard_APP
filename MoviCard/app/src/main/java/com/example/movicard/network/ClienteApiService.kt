package com.example.movicard.network

import com.example.movicard.model.Cliente
import com.example.movicard.model.Suscripcion
import com.example.movicard.model.Ticket
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ClienteApiService {
    @GET("/get/clientes/")
    suspend fun getAllClientes(): List<Cliente>

    @GET("/get/clientes/{id}")
    suspend fun getClienteById(@Path("id") id: Int): Cliente

    // PUT para cambiar todos los datos del cliente excepto la contraseña
    @PUT("/put/cliente/{id}")
    suspend fun actualizarDatosCliente(
        @Path("id") id: Int,
        @Query("nombre") nombre: String,
        @Query("apellido") apellido: String,
        @Query("dni") dni: String,
        @Query("correo") correo: String,
        @Query("telefono") telefono: String,
        @Query("direccion") direccion: String,
        @Query("numero_bloque") numeroBloque: String,
        @Query("numero_piso") numeroPiso: String,
        @Query("codigopostal") codigoPostal: String,
        @Query("ciudad") ciudad: String,
        @Query("password") password: String
    ): Response<Unit>

    // PUT para cambiar solo la contraseña
    @PUT("/put/cliente/password/{id}")
    suspend fun actualizarPassword(
        @Path("id") id: Int,
        @Query("actual") actualPassword: String,
        @Query("nueva") nuevaPassword: String
    ): Response<Unit>

    // GET suscripción de un cliente
    @GET("/get/suscripcion/{id_cliente}")
    suspend fun getSuscripcion(@Path("id_cliente") idCliente: Int): Suscripcion

    // POST suscripción (por si necesitas crearla manualmente)
    @POST("/post/suscripcion/")
    suspend fun createSuscripcion(@Query("id_cliente") idCliente: Int): Suscripcion

    // PUT actualizar suscripción a PREMIUM
    @PUT("/put/suscripcion/{id_cliente}")
    suspend fun actualizarSuscripcion(@Path("id_cliente") idCliente: Int): Suscripcion

    @GET("/get/tickets/{id}")
    suspend fun getTicketByClienteId(@Path("id") clienteId: Int): Ticket

    @POST("/post/tickets/")
    suspend fun crearTicket(@Query("tipo") tipo: String, @Query("id_cliente") idCliente: Int)

    @PUT("/put/tickets/{id}")
    suspend fun updateTicket(@Path("id") id: Int, @Body tipo: String): Response<Unit>

}