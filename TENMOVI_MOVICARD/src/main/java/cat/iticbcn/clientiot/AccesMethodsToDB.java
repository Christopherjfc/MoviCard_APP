package cat.iticbcn.clientiot;

import cat.iticbcn.clientiot.api.ApiClient;
import cat.iticbcn.clientiot.modelo.Ticket;
import cat.iticbcn.clientiot.service.TicketService;
import retrofit2.Call;
import retrofit2.Response;

public class AccesMethodsToDB {

    private final TicketService ticketService;

    public AccesMethodsToDB() {
        this.ticketService = ApiClient.getService();
    }

    public void verificarYActualizarTicket(int idCliente) {
        try {
            Call<Ticket> call = ticketService.getTicket(idCliente);
            Response<Ticket> response = call.execute();
    
            if (!response.isSuccessful() || response.body() == null) {
                new DispositiuIot().enviarMensajeToAws("El cliente no tiene ticket asignado.");
                return;
            }
    
            Ticket ticket = response.body();
            String tipo = ticket.getTipo();
            int cantidad = ticket.getCantidad();
            
            if ("TENMOVI".equals(tipo)) {
                if (cantidad > 0) {
                    ticket.setCantidad(cantidad - 1);
                    Call<Void> updateCall = ticketService.updateTicket(idCliente, ticket);
                    Response<Void> updateResponse = updateCall.execute();
    
                    if (updateResponse.isSuccessful()) {
                        new DispositiuIot().enviarMensajeToAws("Saldo TENMOVI actualizado: " + (cantidad - 1));
                    } else {
                        new DispositiuIot().enviarMensajeToAws("Error al actualizar ticket TENMOVI.");
                    }
                } else {
                    new DispositiuIot().enviarMensajeToAws("Saldo agotado. No se realizó ningún cambio.");
                }
            } else if ("MOVIMES".equals(tipo) || "TRIMOVI".equals(tipo)) {
                new DispositiuIot().enviarMensajeToAws("Lectura válida para ticket ilimitado: " + tipo);
            } else {
                new DispositiuIot().enviarMensajeToAws("Tipo de ticket desconocido.");
            }
    
        } catch (Exception e) {
            System.err.println("Error HTTP/API: " + e.getMessage());
        }
    }    
}
