package models.BatchCluster;

public enum BatchClusterState {
  READY("ready"),
  EXECUTING("executing"),
  FINISHED("finished");

  private String stateName;

  private BatchClusterState(String stateName) {
    this.stateName = stateName;
  }

  public String getStateName() {
    return stateName;
  }
}
