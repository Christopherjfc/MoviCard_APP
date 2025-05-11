package cat.iticbcn.clientiot;

import org.json.JSONObject;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;

public class TopicIoT  extends AWSIotTopic {
    private static long ultimaEjecucion = 0;
    private static final long INTERVAL_MS = 1000; // 1s entre ejecuciones
        
    public TopicIoT(String topic, AWSIotQos qos) {
        super(topic, qos);
    }

    @Override
    public void onMessage(AWSIotMessage message) {
        try {
            String payload = message.getStringPayload();
            JSONObject json = new JSONObject(payload);

            // Solo ejecutar si tiene campo "uid"
            if (!json.has("uid")) {
                System.out.println("Mensaje ignorado: no contiene UID");
                return;
            }

            long ahora = System.currentTimeMillis();

            // Si han pasado menos de X ms desde la última ejecución, ignorar
            if (ahora - ultimaEjecucion < INTERVAL_MS) {
                System.out.println("Mensaje ignorado por tiempo mínimo entre lecturas.");
                return;
            }

            ultimaEjecucion = ahora;

            // Ejecutar lógica
            AccesMethodsToDB access = new AccesMethodsToDB();
            access.verificarYActualizarTicket(1);

        } catch (Exception e) {
            System.err.println("Error al procesar mensaje MQTT: " + e.getMessage());
        }
    }
}