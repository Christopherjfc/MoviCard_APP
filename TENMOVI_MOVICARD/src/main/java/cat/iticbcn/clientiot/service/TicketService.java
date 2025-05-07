package cat.iticbcn.clientiot.service;

import cat.iticbcn.clientiot.modelo.Ticket;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface TicketService {

    @GET("/get/tickets/{id_cliente}")
    Call<Ticket> getTicket(@Path("id_cliente") int idCliente);

    @PUT("/put/tickets/cantidad/{id_cliente}")
    Call<Void> updateTicket(@Path("id_cliente") int idCliente, @Body Ticket ticket);
}
