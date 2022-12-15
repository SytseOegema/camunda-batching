package io.camunda.connector;

import io.camunda.connector.api.annotation.Secret;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import java.util.Objects;

public class MyConnectorRequest {

  @NotEmpty
  private String host;

  @NotEmpty
  private String package;

  @NotEmpty
  private String functionName;

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getPackage() {
    return package;
  }

  public void setPackage(String package) {
    this.package = package;
  }


  public String getFunctionName() {
    return functionName;
  }

  public void setFunctionName(String functionName) {
    this.functionName = functionName;
  }

  @Override
  public int hashCode() {
    return Objects.hash(package + host + functionName);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    MyConnectorRequest other = (MyConnectorRequest) obj;
    return Objects.equals(message, other.message);
  }

  @Override
  public String toString() {
    return "MyConnectorRequest [host="
      + host
      + " - package="
      + package
      + " - functionName="
      + functionName
      + "]";
  }
}
