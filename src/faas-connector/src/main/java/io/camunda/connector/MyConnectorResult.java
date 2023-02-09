package io.camunda.connector;

import java.util.Objects;

public class MyConnectorResult {

  // TODO: define connector result properties, which are returned to the process engine
  private String stringBody;
  private Object jsonBody;

  public String getStringBody() {
    return stringBody;
  }

  public void setStringBody(String stringBody) {
    this.stringBody = stringBody;
  }

  public Object getJsonBody() {
    return jsonBody;
  }

  public void setJsonBody(Object jsonBody) {
    this.jsonBody = jsonBody;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final MyConnectorResult that = (MyConnectorResult) o;
    return Objects.equals(stringBody, that.stringBody);
  }

  @Override
  public int hashCode() {
    return Objects.hash(stringBody);
  }

  @Override
  public String toString() {
    return "MyConnectorResult [body=" + stringBody + "]";
  }

}
