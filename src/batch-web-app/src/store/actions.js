import axios from "axios";

const actions = {
  getProcesses(context) {
    return axios({
      method: 'get',
      url: `${process.env.VUE_APP_BATCH_CONTROLLER_API}/processes`,
    })
      .then((response) => {
        context.commit("setProcesses", response.data);
        return Promise.resolve();
      })
      .catch((error) => {
        alert(error);
        return Promise.reject(error);
      });
  },
  createProcess(context, data) {
    return axios({
      method: 'post',
      url: `${process.env.VUE_APP_BATCH_CONTROLLER_API}/processes`,
      data,
    })
      .then(() => {
        context.dispatch('getProcesses');
        return Promise.resolve();
      })
      .catch((error) => {
        alert(error);
        return Promise.reject(error);
      });
  },
  getProcessInstances(context) {
    return axios({
      method: 'get',
      url: `${process.env.VUE_APP_BATCH_CONTROLLER_API}/process-instances`,
    })
      .then((response) => {
        context.commit("setProcessInstances", response.data);
        return Promise.resolve();
      })
      .catch((error) => {
        alert(error);
        return Promise.reject(error);
      });
  },
  createProcessInstance(context, payload) {
    return axios({
      method: 'post',
      url: `${process.env.VUE_APP_BATCH_CONTROLLER_API}/processes/${payload.processId}/create-instance`,
      data: payload.data,
    })
      .then(() => {
        context.dispatch("getProcessInstances");
        return Promise.resolve();
      })
      .catch((error) => {
        alert(error);
        return Promise.reject(error);
      });
  },
  getBatchModels(context) {
    return axios({
      method: 'get',
      url: `${process.env.VUE_APP_BATCH_CONTROLLER_API}/batch-models`,
    })
      .then((response) => {
        context.commit("setBatchModels", response.data);
        return Promise.resolve();
      })
      .catch((error) => {
        alert(error);
        return Promise.reject(error);
      });
  },
  createBatchModel(context, data) {
    return axios({
      method: 'post',
      url: `${process.env.VUE_APP_BATCH_CONTROLLER_API}/batch-models`,
      data,
    })
      .then(() => {
        context.dispatch('getBatchModels');
        return Promise.resolve();
      })
      .catch((error) => {
        alert(error);
        return Promise.reject(error);
      });
  },
  deleteBatchModel(context, batchModelId) {
    return axios({
      method: 'delete',
      url: `${process.env.VUE_APP_BATCH_CONTROLLER_API}/batch-models/${batchModelId}`,
    })
      .then(() => {
        context.dispatch('getBatchModels');
        return Promise.resolve();
      })
      .catch((error) => {
        alert(error);
        return Promise.reject(error);
      });
  },
  getBatchActivityConnectors(context) {
    return axios({
      method: 'get',
      url: `${process.env.VUE_APP_BATCH_CONTROLLER_API}/batch-activity-connectors`,
    })
      .then((response) => {
        context.commit("setBatchActivityConnectors", response.data);
        return Promise.resolve();
      })
      .catch((error) => {
        alert(error);
        return Promise.reject(error);
      });
  },
  createBatchActivityConnectors(context, data) {
    return axios({
      method: 'post',
      url: `${process.env.VUE_APP_BATCH_CONTROLLER_API}/batch-activity-connectors`,
      data,
    })
      .then(() => {
        context.dispatch('getBatchActivityConnectors');
        return Promise.resolve();
      })
      .catch((error) => {
        alert(error);
        return Promise.reject(error);
      });
  },
  deleteBatchActivityConnectors(context, connectorId) {
    return axios({
      method: 'delete',
      url: `${process.env.VUE_APP_BATCH_CONTROLLER_API}/batch-activity-connectors/${connectorId}`,
    })
      .then(() => {
        context.dispatch('getBatchActivityConnectors');
        return Promise.resolve();
      })
      .catch((error) => {
        alert(error);
        return Promise.reject(error);
      });
  },
};

export default actions;
