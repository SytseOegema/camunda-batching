import com.google.gson.JsonObject;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

public class MyFunction {
    public static JsonObject main(JsonObject args){
        String testVariable;

        try {
            testVariable = args.getAsJsonPrimitive("testVariable").getAsString();
        } catch(Exception e) {
            testVariable = "Unable to get testVariable from request body";
        }

        JsonObject response = new JsonObject();
        response.addProperty("result", "Able to read tesetvariable: " + testVariable + "!");
        return response;
    }
}
