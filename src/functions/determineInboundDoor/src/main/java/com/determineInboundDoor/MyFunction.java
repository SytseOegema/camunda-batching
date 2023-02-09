import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// This function determines optimal door assignment in a crossdock process.
//
// It assume 3 inbound and 3 outbound doors and a maximum of 3 vehicles.
public class MyFunction {
  static InboundDoorPlanning planning = new InboundDoorPlanning();

  public static JsonObject main(JsonObject args) {
    JsonObject response = new JsonObject();
    response.addProperty("result", "Argument was not in the expected format.");
    if (args.get("content").isJsonObject()) {
      if (args.getAsJsonObject("content").get("goods").isJsonArray()) {
        response = determineInboundDoor(args.getAsJsonObject("content"));
      }
    } else if (args.get("content").isJsonArray()) {
      response = determineInboundDoor(args.getAsJsonArray("content"));
    }
    return response;
  }

  private static JsonObject determineInboundDoor(JsonArray vehicles) {
    for (int vehicleIdx = 1; vehicleIdx <= vehicles.size(); vehicleIdx++) {
      for (int doorIdx = 1; doorIdx <= 3; doorIdx++) {
        int distance = calculateDistance(
          vehicles.get(vehicleIdx - 1).getAsJsonObject().getAsJsonArray("goods"),
          doorIdx);
        planning.addDistance(vehicleIdx, doorIdx, distance);
      }
    }

    Combo result = calculateInboundDoorSolution(planning);
    JsonObject vehicle1 = new JsonObject();
    vehicle1.addProperty("inboundDoor", result.door1);
    vehicle1.addProperty(
      "licensePlate",
      vehicles.get(result.vehicle1 - 1).getAsJsonObject().get("licensePlate").getAsString());

    JsonObject vehicle2 = new JsonObject();
    vehicle2.addProperty("inboundDoor", result.door2);
    vehicle2.addProperty(
      "licensePlate",
      vehicles.get(result.vehicle2 - 1).getAsJsonObject().get("licensePlate").getAsString());


    JsonObject vehicle3 = new JsonObject();
    vehicle3.addProperty("inboundDoor", result.door3);
    vehicle3.addProperty(
      "licensePlate",
      vehicles.get(result.vehicle3 - 1).getAsJsonObject().get("licensePlate").getAsString());

    JsonArray array = new JsonArray();
    array.add(vehicle1);
    array.add(vehicle2);
    array.add(vehicle3);

    JsonObject response = new JsonObject();
    response.add("result", array);
    return response;
  }

  private static Combo calculateInboundDoorSolution(InboundDoorPlanning planning) {
    // add distance for all 3*2*1 options.
    List<Combo> combos = new ArrayList<>();

    combos.add(new Combo(1, 1, 2, 2, 3, 3, planning));
    combos.add(new Combo(1, 1, 2, 3, 3, 2, planning));

    combos.add(new Combo(1, 2, 2, 1, 3, 3, planning));
    combos.add(new Combo(1, 2, 2, 3, 3, 1, planning));

    combos.add(new Combo(1, 3, 2, 1, 3, 2, planning));
    combos.add(new Combo(1, 3, 2, 2, 3, 1, planning));

    int minIdx = -1;
    int minVal = 100000;
    for (int idx = 0; idx < combos.size(); idx++) {
      if (combos.get(idx).distance < minVal) {
        minIdx  = idx;
        minVal = combos.get(idx).distance;
      }
    }

    return combos.get(minIdx);
  }


  // Determines the most optimal inbound door for a single vehicle without
  // considering other vehicles.
  private static JsonObject determineInboundDoor(JsonObject vehicle) {
    int bestDoor = 0;
    int bestDoorDistance = 100000;
    for (int idx = 1; idx <= 3; idx++) {
      int distance = calculateDistance(vehicle.getAsJsonArray("goods"), idx);
      if (distance < bestDoorDistance) {
        bestDoorDistance = distance;
        bestDoor = idx;
      }
    }
    JsonObject response = new JsonObject();
    response.addProperty("inboundDoor", bestDoor);
    return response;
  }

  // Calculates the simple distance from ingoing to outbound door.
  // Distance per good is the absolute difference between the inbound door
  // number and outboud door number.
  private static Integer calculateDistance(JsonArray goods, Integer inboundDoorNumber) {
    int distance = 0;
    for (int idx = 0; idx < goods.size(); idx++) {
      JsonObject good = goods.get(idx).getAsJsonObject();
      distance += Math.abs(good.get("outboundDoor").getAsInt() - inboundDoorNumber);
    }
    return distance;
  }

  private static class Combo {
    public int vehicle1;
    public int door1;
    public int vehicle2;
    public int door2;
    public int vehicle3;
    public int door3;
    public int distance;

    public Combo(
      int vehicle1,
      int door1,
      int vehicle2,
      int door2,
      int vehicle3,
      int door3,
      InboundDoorPlanning planning
    ) {
      this.vehicle1 = vehicle1;
      this.door1 = door1;
      this.vehicle2 = vehicle2;
      this.door2 = door2;
      this.vehicle3 = vehicle3;
      this.door3 = door3;
      this.distance = planning.getDistance(vehicle1, door1)
        + planning.getDistance(vehicle2, door2)
        + planning.getDistance(vehicle3, door3);
    }
  }

  private static class InboundDoorPlanning {
    private InboundDoorPlanningNode[] planningArray = new InboundDoorPlanningNode[10];

    public void addDistance(int vehicle, int door, int distance) {
      planningArray[index(vehicle, door)] =
        new InboundDoorPlanningNode(vehicle, door, distance);
    }

    public int getDistance(int vehicle, int door) {
      return planningArray[index(vehicle, door)].distance;
    }

    private int index(int vehicle, int door) {
      return (vehicle - 1) * 3 + door;
    }

    private class InboundDoorPlanningNode {
      public int vehicle;
      public int door;
      public int distance;

      public InboundDoorPlanningNode(int vehicle, int door, int distance) {
        this.vehicle = vehicle;
        this.door = door;
        this.distance = distance;
      }
    }
  }
}
