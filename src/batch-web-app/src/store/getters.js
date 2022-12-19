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
  }
};

export default getters;
