package cat.iticbcn.clientiot;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HexFormat;

import org.json.JSONObject;

import com.amazonaws.services.iot.client.AWSIotMessage;

public class AccesMethodsToDB {

    public String creaHashPsswd(String password) throws Exception{
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
        HexFormat hex = HexFormat.of();
        String hash = hex.formatHex(bytes); 
        return hash; 
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

    public void selectAlumnes (Connection con) {
        String sql = "SELECT * FROM alumne"; // Consulta SQL
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
        
            while (rs.next()) {
                int id = rs.getInt("IdAlumne");
                String nombre = rs.getString("NomAlumne");
                System.out.println("ID: " + id + ", Nom: " + nombre);
            }
        } catch (SQLException e) {
            System.out.println("Error al ejecutar la consulta: " + e.getMessage());
        }
    }

    public void verificarYActualizarTicket(Connection con, int idCliente) {
        String selectTicket = "SELECT tipo, cantidad FROM ticket WHERE id_cliente = ?";
        String updateTicket = "UPDATE ticket SET cantidad = cantidad - 1 WHERE id_cliente = ?";
        
        try (PreparedStatement stmt = con.prepareStatement(selectTicket)) {
            stmt.setInt(1, idCliente);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String tipo = rs.getString("tipo");
                int cantidad = rs.getInt("cantidad");

                if ("TENMOVI".equals(tipo)) {
                    if (cantidad > 0) {
                        try (PreparedStatement updateStmt = con.prepareStatement(updateTicket)) {
                            updateStmt.setInt(1, idCliente);
                            updateStmt.executeUpdate();
                            System.out.println("Nuevo saldo TENMOVI para cliente " + idCliente + ": " + (cantidad - 1));
                            new DispositiuIot().enviarMensajeToAws("Saldo TENMOVI actualizado. Nueva cantidad: " + (cantidad - 1));
                        }
                    } else {
                        System.out.println("El saldo TENMOVI está agotado. No se puede descontar más.");
                        new DispositiuIot().enviarMensajeToAws("Saldo agotado. No se realizó ningún cambio.");
                    }
                } else if ("MOVIMES".equals(tipo) || "TRIMOVI".equals(tipo)) {
                    System.out.println("Lectura de ticket ilimitado (" + tipo + ") para cliente " + idCliente);
                    new DispositiuIot().enviarMensajeToAws("Lectura válida para ticket ilimitado: " + tipo);
                } else {
                    System.out.println("Tipo de ticket desconocido.");
                    new DispositiuIot().enviarMensajeToAws("Tipo de ticket desconocido.");
                }

            } else {
                System.out.println("El cliente " + idCliente + " no tiene un ticket asignado.");
                new DispositiuIot().enviarMensajeToAws("El cliente no tiene ticket asignado.");
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar o actualizar el ticket: " + e.getMessage());
        }
    }
}
