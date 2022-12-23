const getters = {
  getProcesses(state) {
    return state.processes;
  },
  getProcessInstances(state) {
    return state.processInstances;
  },
  getBatchModels(state) {
    return state.batchModels;
  },
  getBatchActivityConnectors(state) {
    return state.batchActivityConnectors;
  },
  getBatchClusters(state) {
    return state.batchClusters;
  }
};

export default getters;
