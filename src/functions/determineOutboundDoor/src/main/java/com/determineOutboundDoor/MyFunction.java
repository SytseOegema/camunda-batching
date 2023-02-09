import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MyFunction {
  public static JsonObject main(JsonObject args) {
    JsonObject response = new JsonObject();
    response.addProperty("result", "Argument was not in the expected format.");
    if (!args.get("content").isJsonObject()) {
      return response;
    } else if (!args.getAsJsonObject("content").get("goods").isJsonArray()) {
      return response;
    }
    JsonArray goods = args.getAsJsonObject("content").getAsJsonArray("goods");

    for (int idx = 0; idx < goods.size(); idx++) {
      JsonObject good = goods.get(idx).getAsJsonObject();
      good.addProperty(
        "outboundDoor",
        determineOutgoingDoor(good.get("postalCode").getAsString())
      );
      goods.set(idx, good);
    }

    response = new JsonObject();
    response.add("outboundDoors", goods);
    return response;
  }

  private static Integer determineOutgoingDoor(String postalCode) {
    int code = Integer.parseInt(postalCode.substring(0, 4));
    // add 3333 to code to get result 1,2 or 3 instead of 0, 1 or 2
    return (code + 3333) / 3333;
  }
}
