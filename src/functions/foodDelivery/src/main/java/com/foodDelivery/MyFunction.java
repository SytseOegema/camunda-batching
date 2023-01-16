import com.google.gson.JsonObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MyFunction {
    public static JsonObject main(JsonObject args) {
        String dish;

        try {
            dish = args.getAsJsonPrimitive("dish").getAsString();
        } catch(Exception e) {
            dish = "random";
        }

        LocalDateTime myDateObj = LocalDateTime.now();
        // compensate for UTC timezone.
        myDateObj = myDateObj.plusMinutes(60);

        JsonObject response = new JsonObject();
        switch (dish) {
          case "Chicken":
              myDateObj = myDateObj.plusMinutes(15);
              break;
          case "Salmon":
              myDateObj = myDateObj.plusMinutes(30);
              break;
           case "Mushroom thingy":
               myDateObj = myDateObj.plusMinutes(90);
               break;
          default:
              myDateObj = myDateObj.plusMinutes(240);
              break;
        }

        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String formattedDate = myDateObj.format(myFormatObj);

        response.addProperty("deliveryTime", "your dish will be delivered at: " + formattedDate);
        return response;
    }
}
