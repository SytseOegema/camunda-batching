package managers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Gson;
import io.camunda.batching.messaging.messages.ActivityType;
import io.camunda.batching.messaging.messages.ProcessInstanceDTO;
import models.BatchActivityConnector.BatchActivityConnectorModel;
import models.BatchActivityConnector.BatchActivityConnectorConditionModel;
import models.ProcessInstanceModel;
import java.util.Map;

public class ProcessInstanceActivityManager {
  public static boolean instanceSatisfiesConnectorConditions(ProcessInstanceDTO instance, BatchActivityConnectorModel connector) {
    JsonObject jsonObject = new JsonParser().parse(instance.variables).getAsJsonObject();
    for (BatchActivityConnectorConditionModel condition : connector.conditions) {
      try {
        if (jsonObject.has(condition.fieldName)) {
          boolean satisfied = isConditionSatisfied(
            condition,
            jsonObject.get(condition.fieldName)
          );
          if (!satisfied) {
            return false;
          }
        } else {
          return false;
        }
      } catch(Exception e) {
        return false;
      }
    }
    return true;
  }

  public static String getVariableValue(ProcessInstanceModel instance, String variableName) {
    JsonObject jsonObject = new JsonParser().parse(instance.variables).getAsJsonObject();
    try {
      if (jsonObject.has(variableName)) {
        return jsonObject.get(variableName).getAsString();
      }
      return "EMPTY_VALUE";
    } catch(Exception e) {
      return "EMPTY_VALUE";
    }
  }

  private static boolean isConditionSatisfied(
      BatchActivityConnectorConditionModel condition,
      JsonElement jsonElem
  ) {
    switch (condition.fieldType) {
      case INT:
        return compare(
          Integer.parseInt(condition.compareValue),
          jsonElem.getAsInt(),
          condition.compareOperator);
      case LONG:
        return compare(
          Long.parseLong(condition.compareValue),
          jsonElem.getAsLong(),
          condition.compareOperator);
      case STRING:
        return compare(
          condition.compareValue,
          jsonElem.getAsString(),
          condition.compareOperator);
      case FLOAT:
        return compare(
          Float.parseFloat(condition.compareValue),
          jsonElem.getAsFloat(),
          condition.compareOperator);
      case DOUBLE:
        return compare(
          Double.parseDouble(condition.compareValue),
          jsonElem.getAsDouble(),
          condition.compareOperator);
      case BOOLEAN:
        return
            Boolean.parseBoolean(condition.compareValue) ==
            jsonElem.getAsBoolean();
      default:
        return compare(
          condition.compareValue,
          jsonElem.getAsString(),
          condition.compareOperator);
    }
  }

  private static boolean compare(
      int conditionValue,
      int instanceValue,
      BatchActivityConnectorConditionModel.CompareOperator operator) {
    switch (operator) {
      case EQUALS:
        return instanceValue == conditionValue;
      case LARGER_THAN:
        return instanceValue > conditionValue;
      case SMALLER_THAN:
        return instanceValue < conditionValue;
      default:
        return instanceValue == conditionValue;
    }
  }

  private static boolean compare(
      long conditionValue,
      long instanceValue,
      BatchActivityConnectorConditionModel.CompareOperator operator) {
    switch (operator) {
      case EQUALS:
        return instanceValue == conditionValue;
      case LARGER_THAN:
        return instanceValue > conditionValue;
      case SMALLER_THAN:
        return instanceValue < conditionValue;
      default:
        return instanceValue == conditionValue;
    }
  }

  private static boolean compare(
      String conditionValue,
      String instanceValue,
      BatchActivityConnectorConditionModel.CompareOperator operator) {
    int value = instanceValue.compareTo(conditionValue);
    switch (operator) {
      case EQUALS:
        return value == 0;
      case LARGER_THAN:
        return value == 1;
      case SMALLER_THAN:
        return value == -1;
      default:
        return value == 0;
    }
  }

  private static boolean compare(
      float conditionValue,
      float instanceValue,
      BatchActivityConnectorConditionModel.CompareOperator operator) {
    switch (operator) {
      case EQUALS:
        return instanceValue == conditionValue;
      case LARGER_THAN:
        return instanceValue > conditionValue;
      case SMALLER_THAN:
        return instanceValue < conditionValue;
      default:
        return instanceValue == conditionValue;
    }
  }

  private static boolean compare(
      double conditionValue,
      double instanceValue,
      BatchActivityConnectorConditionModel.CompareOperator operator) {
    switch (operator) {
      case EQUALS:
        return instanceValue == conditionValue;
      case LARGER_THAN:
        return instanceValue > conditionValue;
      case SMALLER_THAN:
        return instanceValue < conditionValue;
      default:
        return instanceValue == conditionValue;
    }
  }

  public static boolean isActivity(ProcessInstanceDTO message) {
    boolean result = false;
    switch (message.elementType) {
      case SERVICE_TASK:
        result = true;
        break;
      case RECEIVE_TASK:
        result = true;
        break;
      case USER_TASK:
        result = true;
        break;
      case MANUAL_TASK:
        result = true;
        break;
      default:
        result = false;
        break;
    }
    return result;
  }

  public static String updateVariables(String variables, String faasReply) {
    JsonObject variablesObject = new JsonParser().parse(variables).getAsJsonObject();
    JsonObject replyObject = new JsonParser().parse(faasReply).getAsJsonObject();


    for (Map.Entry<String, JsonElement> entry : replyObject.entrySet()) {
      System.out.println(entry.getKey() + "/" + entry.getValue());
      variablesObject.add(entry.getKey(), entry.getValue());
      // if(entry.getValue().isJsonArray()) {
      //   variablesObject.add(entry.getKey(), entry.getValue().getAsJsonArray());
      // } else if (entry.getValue().isJsonObject()) {
      //   variablesObject.add(entry.getKey(), entry.getValue().getAsJsonObject());
      // } else {
      //   variablesObject.addProperty(entry.getKey(), entry.getValue().toString());
      // }
    }

    Gson gson = new Gson();
    System.out.println(gson.toJson(variablesObject));
    return gson.toJson(variablesObject);
  }
}
