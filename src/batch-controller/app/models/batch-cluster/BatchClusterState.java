package models.BatchCluster;

public enum BatchClusterState {
  READY("ready"),
  RESUMING("resuming"),
  EXECUTING("executing"),
  RESUMED("resumed"),
  FINISHED("finished");

  private String stateName;

  private BatchClusterState(String stateName) {
    this.stateName = stateName;
  }

  public String getStateName() {
    return stateName;
  }
}
