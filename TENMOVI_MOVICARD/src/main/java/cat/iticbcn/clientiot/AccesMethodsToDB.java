package cat.iticbcn.clientiot;

import org.json.JSONObject;
import com.amazonaws.services.iot.client.AWSIotMessage;
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

    public String getNUIDHex(AWSIotMessage message) {
        try {
            // Obtener el payload como cadena
            String payload = message.getStringPayload();
            
            // Convertir el payload en un objeto JSON
            JSONObject jsonObject = new JSONObject(payload);
            // Extraer el valor de la clave "NUID_Hex"
            if (jsonObject.has("uid")) {
                String uidTarget = jsonObject.getString("uid");
                System.out.println("\n");
                System.out.println(uidTarget);
                System.out.println("\n");
                return uidTarget.strip();
            } else {
                return "Error: Clave 'uid_target' no encontrada en el mensaje.";
            }
        } catch (Exception e) {
            return "Error al procesar el mensaje: " + e.getMessage();
        }
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
            System.out.println(tipo);
            int cantidad = ticket.getCantidad();
            
            if ("TENMOVI".equals(tipo)) {
                System.out.println("Entró a la condición de TENMOVI " + tipo);
                if (cantidad > 0) {
                    System.out.println("Entró a la condición de TENMOVI cantidad mayor que " + cantidad);
                    ticket.setCantidad(cantidad - 1);
                    System.out.println("Entró a la condición de TENMOVI cambio de cantidad " + cantidad);
                    Call<Void> updateCall = ticketService.updateTicket(idCliente, ticket);
                    Response<Void> updateResponse = updateCall.execute();
                    System.out.println("Entró a la condición de TENMOVI" + cantidad);
    
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
