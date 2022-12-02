/*
 * Messaging implementation
 *
 * Created by Sytse Oegema
 */
package io.camunda.batching.messaging.messages;

public enum ProcessInstanceIntent {
  READY_ELEMENT("readyElement"),
  COMPLETE_ELEMENT("completeElement"),
  TERMINATE_ELEMENT("terminateElement"),
  RESUME_ELEMENT("resumeElement"),
  UNSPECIFIED("unspecified");

  private String value;

  private ProcessInstanceIntent(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
