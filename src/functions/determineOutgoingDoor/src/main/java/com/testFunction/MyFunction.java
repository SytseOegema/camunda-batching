import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MyFunction {
    public static JsonObject main(JsonObject args) {
        // return args;

        // String dish;
        //
        // try {
        //     dish = args.getAsJsonPrimitive("dish").getAsString();
        // } catch(Exception e) {
        //     dish = "random";
        // }
        //
        // LocalDateTime myDateObj = LocalDateTime.now();
        // // compensate for UTC timezone.
        // myDateObj = myDateObj.plusMinutes(60);
        //
        JsonObject response = new JsonObject();
        // switch (dish) {
        //   case "Chicken":
        //       myDateObj = myDateObj.plusMinutes(15);
        //       break;
        //   case "Salmon":
        //       myDateObj = myDateObj.plusMinutes(30);
        //       break;
        //    case "Mushroom thingy":
        //        myDateObj = myDateObj.plusMinutes(90);
        //        break;
        //   default:
        //       myDateObj = myDateObj.plusMinutes(240);
        //       break;
        // }
        //
        // DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        // String formattedDate = myDateObj.format(myFormatObj);
        //
        if (args.get("content").isJsonArray()) {
          response.addProperty("type", "array");
        }
        if (args.get("content").isJsonObject()) {
          response.addProperty("type", "object");
        }

        return response;
    }
}
