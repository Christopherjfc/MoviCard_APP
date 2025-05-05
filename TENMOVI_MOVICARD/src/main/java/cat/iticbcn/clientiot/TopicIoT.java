package cat.iticbcn.clientiot;

import java.sql.Connection;
import java.sql.SQLException;


import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;

public class TopicIoT  extends AWSIotTopic {
    static final String url = "jdbc:postgresql://192.168.27.5:5432/movicard"; 
    static final String usuario = "christopher_psql"; 
    static final String contrasena = "postgres"; 

    public TopicIoT(String topic, AWSIotQos qos) {
        super(topic, qos);
    }

    @Override
    public void onMessage(AWSIotMessage message) {

        // Si el payload es "insert ok", se asigna null a lastMessage
        if (!"insert ok".equals(message.getStringPayload())) { // Comparación correcta de cadenas
          System.out.println("\n");
          creaRegistro(message);
          System.out.println("\n");
        } 
    }
    
    public void creaRegistro(AWSIotMessage mensajeIoT){
        System.out.println("\nMensaje recibido en el main: " + mensajeIoT.getStringPayload());
    
        try (Connection con = ConnectDB.getConnection(url, usuario, contrasena)) {
            System.out.println("Se conecta a la base de datos");
            AccesMethodsToDB access = new AccesMethodsToDB();
            access.verificarYActualizarTicket(con, 2); // ID cliente hardcoded como 2 según requerimiento
        } catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        } catch(Exception e){
            System.err.println("Error IOT: "+e.getLocalizedMessage());
            System.exit(-1);
        }
    }
}