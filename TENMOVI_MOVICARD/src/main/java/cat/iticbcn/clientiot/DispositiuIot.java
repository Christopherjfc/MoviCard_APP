package cat.iticbcn.clientiot;

import org.json.JSONObject;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.sample.sampleUtil.SampleUtil;
import com.amazonaws.services.iot.client.sample.sampleUtil.SampleUtil.KeyStorePasswordPair;

public class DispositiuIot{

    private static final String FICH_CLAU_PRIVADA = "C:\\Users\\Christopher\\Documents\\Proyecto_MoviCard\\MoviCard_APP\\TENMOVI_MOVICARD\\client1certs\\private.pem.key";
    private static final String FICH_CERT_DISP_IOT = "C:\\Users\\Christopher\\Documents\\Proyecto_MoviCard\\MoviCard_APP\\TENMOVI_MOVICARD\\client1certs\\certidicado.pem.crt";
    private static final String ENDPOINT = "a36fz4jf3lbj4d-ats.iot.us-east-1.amazonaws.com";
    public static final String TOPIC = "ESP32";
    public static final String CLIENT_ID = "ESP32_RFID";
    public static final AWSIotQos TOPIC_QOS = AWSIotQos.QOS0;
    private static AWSIotMqttClient awsIotClient;

    public static void setClient(AWSIotMqttClient cli) {
        awsIotClient = cli;
    }

    public static void initClient() {
        String cliEP = ENDPOINT;
        String cliId = CLIENT_ID;

        String certFile = FICH_CERT_DISP_IOT;
        String pKFile = FICH_CLAU_PRIVADA;

        if (awsIotClient == null && certFile != null && pKFile != null) {
            String algorithm = null;
            KeyStorePasswordPair pair = SampleUtil.getKeyStorePasswordPair(certFile, pKFile, algorithm);
            awsIotClient = new AWSIotMqttClient(cliEP, cliId, pair.keyStore, pair.keyPassword);
        }

        if (awsIotClient == null) {
            throw new IllegalArgumentException("Error als construir client amb el certificat o les credencials.");
        }
    }

    public void conecta() throws AWSIotException{
        initClient();
        awsIotClient.connect();
        try {    
            System.out.println("Cliente Conetado!!!");
        } catch (Exception e) {
            System.err.println("ERROR AL CONCECTARSE!!!");
        }
    }

    public void subscriu() throws AWSIotException{
        TopicIoT topic= new TopicIoT(TOPIC, TOPIC_QOS);
        try {
            awsIotClient.subscribe(topic, true);
            System.out.println("Cliente suscrito!!!");
        } catch (Exception e) {
            System.err.println("ERROR AL SUSCRIBIRSE!!!");
        }
    }

    public void enviarMensajeToAws(String message) {
        try {
            JSONObject jsonMessage = new JSONObject();
            jsonMessage.put("message", message);

            String jsonString = jsonMessage.toString();

            AWSIotMessage iotMessage = new AWSIotMessage(DispositiuIot.TOPIC, DispositiuIot.TOPIC_QOS, jsonString);

            awsIotClient.publish(iotMessage);

            System.out.println("Mensaje enviado a AWS IoT: " + jsonString);
        } catch (Exception e) {
            System.out.println("Error al enviar mensaje a AWS IoT: " + e.getMessage());
        }
    }
}