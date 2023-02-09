package io.camunda.connector;

import io.camunda.connector.api.annotation.Secret;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.google.gson.JsonObject;
import java.util.Objects;

public class MyConnectorRequest {

  @NotEmpty
  private String host;

  @NotEmpty
  private String packageName;

  @NotEmpty
  private String functionName;

  private JsonObject body;

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public String getFunctionName() {
    return functionName;
  }

  public void setFunctionName(String functionName) {
    this.functionName = functionName;
  }

  public void setBody(JsonObject body) {
    this.body = body;
  }

  public String getBody() {
    return body.toString();
  }

  @Override
  public int hashCode() {
    return Objects.hash(packageName + host + functionName + body);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    MyConnectorRequest other = (MyConnectorRequest) obj;
    return Objects.equals(hashCode(), other.hashCode());
  }

  @Override
  public String toString() {
    return "MyConnectorRequest [host="
      + host
      + " - packageName="
      + packageName
      + " - functionName="
      + functionName
      + "]";
  }
}
