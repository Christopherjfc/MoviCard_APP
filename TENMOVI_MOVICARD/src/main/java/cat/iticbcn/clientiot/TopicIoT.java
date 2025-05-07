package cat.iticbcn.clientiot;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;

public class TopicIoT  extends AWSIotTopic {
    public TopicIoT(String topic, AWSIotQos qos) {
        super(topic, qos);
    }

    @Override
    public void onMessage(AWSIotMessage message) {
        System.out.println("Mensaje recibido: " + message.getStringPayload());
            AccesMethodsToDB access = new AccesMethodsToDB();
            access.verificarYActualizarTicket(1); // Cliente ID fijo = 2
    }
}