package io.camunda.connector;

import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import kong.unirest.Unirest;
import kong.unirest.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OutboundConnector(
    name = "MYCONNECTOR",
    inputVariables = {"myProperty", "authentication"},
    type = "io.camunda:template:1")
public class MyConnectorFunction implements OutboundConnectorFunction {

  private static final Logger LOGGER = LoggerFactory.getLogger(MyConnectorFunction.class);

  @Override
  public Object execute(OutboundConnectorContext context) throws Exception {
    var connectorRequest = context.getVariablesAsType(MyConnectorRequest.class);

    context.validate(connectorRequest);
    context.replaceSecrets(connectorRequest);

    return executeConnector(connectorRequest);
  }

  private MyConnectorResult executeConnector(final MyConnectorRequest connectorRequest) {
    LOGGER.info("Executing my connector with request {}", connectorRequest);
    // always uses Openwhisk namespace 'guest'
    String url = connectorRequest.getHost()
      + "/api/v1/web/guest/"
      + connectorRequest.getPackageName()
      + "/"
      + connectorRequest.getFunctionName()
      + ".json";

    try {
      Unirest.config().verifySsl(false);
      HttpResponse<String> response = Unirest.post(url)
        .header("Authorization", "Basic Nzg5YzQ2YjEtNzFmNi00ZWQ1LThjNTQtODE2YWE0ZjhjNTAyOmFiY3pPM3haQ0xyTU42djJCS0sxZFhZRnBYbFBrY2NPRnFtMTJDZEFzTWdSVTRWck5aOWx5R1ZDR3VNREdJd1A=")
        .header("Content-Type", "application/json")
        .body(connectorRequest.getBody())
        .asString();
      System.out.println(response.getBody());
      var result = new MyConnectorResult();
      result.setMyProperty(response.getBody());
      LOGGER.info("Result {}", result);
      return result;
    }
    catch (Exception e) {
      LOGGER.error(e.getMessage());
      var result = new MyConnectorResult();
      result.setMyProperty("Something went wrong");
      return result;
    }
  }
}
