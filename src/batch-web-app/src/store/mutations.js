const mutations = {
  setProcesses(state, processes) {
    state.processes = processes;
    state.processes.forEach((process) => {
      process.activities.forEach((activity) => {
        activity.connectors = [];
      });
    });
  },
  setProcessInstances(state, processInstances) {
    state.processInstances = processInstances;
  },
  setBatchModels(state, batchModels) {
    state.batchModels = batchModels;
  },
  setBatchActivityConnectors(state, batchActivityConnectors) {
    state.batchActivityConnectors = batchActivityConnectors;

    state.processes.forEach((process) => {
      process.activities.forEach((activity) => {
        activity.connectors = [];
        batchActivityConnectors.forEach((con) => {
          if (con.activityId === activity.id) {
            activity.connectors.push(con);
          }
        });
      });
    });
  },
  setBatchClusters(state, batchClusters) {
    state.batchClusters = batchClusters;
  }
};

export default mutations;
