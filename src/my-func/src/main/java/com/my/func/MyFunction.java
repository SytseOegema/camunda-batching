import com.google.gson.JsonObject;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

public class MyFunction {
    public static JsonObject main(JsonObject args){
        String foodPreference;
        String[] menu = {"Chicken", "Salmon", "Mushroom thingy"};

        try {
            foodPreference = args.getAsJsonPrimitive("foodPreference").getAsString();
        } catch(Exception e) {
            foodPreference = "random";
        }


        String foodOrder = menu[0];
        JsonObject response = new JsonObject();
        switch (foodPreference) {
          case "random":
             foodOrder = menu[ThreadLocalRandom.current().nextInt(min, max + 1)];
             break;
          case "meat":
            foodOrder = menu[0];
            break;
           case "fish":
             foodOrder = menu[0];
             break;
          default:
            foodOrder = menu[ThreadLocalRandom.current().nextInt(min, max + 1)];
            break;
        }
        response.addProperty("foodOrder", foodOrder);
        return response;
    }
}
