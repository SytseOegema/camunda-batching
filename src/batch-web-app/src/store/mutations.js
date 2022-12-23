const mutations = {
  setProcesses(state, processes) {
    state.processes = processes;
  },
  setProcessInstances(state, processInstances) {
    state.processInstances = processInstances;
  },
  setBatchModels(state, batchModels) {
    state.batchModels = batchModels;
  },
  setBatchActivityConnectors(state, batchActivityConnectors) {
    state.batchActivityConnectors = batchActivityConnectors;
  },
  setBatchClusters(state, batchClusters) {
    state.batchClusters = batchClusters;
  }
};

export default mutations;
