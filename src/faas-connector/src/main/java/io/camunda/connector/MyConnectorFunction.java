package io.camunda.connector;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Gson;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import kong.unirest.Unirest;
import kong.unirest.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

@OutboundConnector(
    name = "MYCONNECTOR",
    inputVariables = {"host", "packageName", "functionName", "body"},
    type = "io.camunda:template:1")
public class MyConnectorFunction implements OutboundConnectorFunction {

  private static final Logger LOGGER = LoggerFactory.getLogger(MyConnectorFunction.class);

  @Override
  public Object execute(OutboundConnectorContext context) throws Exception {
    var connectorRequest = context.getVariablesAsType(MyConnectorRequest.class);

    context.validate(connectorRequest);
    context.replaceSecrets(connectorRequest);

    return executeConnector(context.getVariables());
  }

  private MyConnectorResult executeConnector(final String json) {
    JsonObject jsonObject = new JsonParser()
      .parse(json)
      .getAsJsonObject();
    // always uses Openwhisk namespace 'guest'
    String url = jsonObject.get("host").getAsString()
      + "/api/v1/web/guest/"
      + jsonObject.get("packageName").getAsString()
      + "/"
      + jsonObject.get("functionName").getAsString()
      + ".json";

    LOGGER.info(url);

    try {
      JsonObject body = new JsonObject();
      body.add("content", jsonObject.getAsJsonObject("body"));

      LOGGER.info(body.toString());

      Unirest.config().verifySsl(false);
      LOGGER.info(".....1......");
      HttpResponse<String> response = Unirest.post(url)
        .header("Authorization", "Basic Nzg5YzQ2YjEtNzFmNi00ZWQ1LThjNTQtODE2YWE0ZjhjNTAyOmFiY3pPM3haQ0xyTU42djJCS0sxZFhZRnBYbFBrY2NPRnFtMTJDZEFzTWdSVTRWck5aOWx5R1ZDR3VNREdJd1A=")
        .header("Content-Type", "application/json")
        .body(body.toString())
        .asString();
      JsonObject replyObject = new JsonParser().parse(response.getBody()).getAsJsonObject();

      var result = new MyConnectorResult();
      Gson gson = new Gson();
      for (Map.Entry<String, JsonElement> entry : replyObject.entrySet()) {
        LOGGER.info(entry.getKey() + "/" + entry.getValue());
        // if (entry.getValue().isJsonArray()) {
        //   result.setJsonBody(gson.fromJson(entry.getValue().toString(), Object.class));
        // } else if (entry.getValue().isJsonObject()) {
        //   // result.setJsonBody(entry.getValue().getAsJsonObject());
        // }
        result.setJsonBody(gson.fromJson(entry.getValue().toString(), Object.class));
        result.setStringBody(entry.getValue().toString());
      }

      LOGGER.info("Result {}", result);
      return result;
    }
    catch (Exception e) {
      LOGGER.error(e.getMessage());
      var result = new MyConnectorResult();
      result.setStringBody("Something went wrong");
      return result;
    }
  }
}
