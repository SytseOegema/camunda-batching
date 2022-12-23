<template>
  <div class="va-h3">
    Batch Clusters
  </div>
  <div class="row">
    <div class="flex md6" v-for="cluster in clusters" :key="cluster.batchClusterId">
      <va-card>
        <div class="row my-3">
          <div class="va-title ml-3">
            Model : {{ getBatchModel(cluster.batchModelId).name }}
          </div>
          <va-badge
            class="ml-auto mr-3"
            :text="cluster.state"
            color="info"
          />
        </div>
        <div class="row ml-3">
          Executed in
          <va-badge
            class="ml-3 mr-3"
            :text="getTimeTillExecute(cluster) + ' minutes'"
            color="primary"
          />
          or with
          <va-badge
            class="ml-3 mr-3"
            :text="getExtraInstance(cluster) + ' instances'"
            color="primary"
          />
          more.
        </div>
        <div class="row ml-3">
          Instances:
        </div>
        <div class="row ml-3" v-for="instance in cluster.instances" :key="instance.processInstanceId">
          <div class="flex md6">
            {{ instance.processInstanceKey }}
          </div>
          <va-button class="flex md6" preset="secondary" border-color="primary" @click="resume(instance.processInstanceId)">
            Resume {{ instance.processInstanceKey }}
          </va-button>
        </div>
      </va-card>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useStore } from "vuex";
import { useToast } from 'vuestic-ui'

const store = useStore();
const { init } = useToast();

const clusters = computed(() => store.getters["getBatchClusters"]);

const getBatchModel = (batchModelId) => {
  const models = store.getters["getBatchModels"];
  return models.find((model) => model.batchModelId === batchModelId);
}

const getTimeTillExecute = (cluster) => {
  const extraTime = getBatchModel(cluster.batchModelId).activationThresholdTime;
  const oldTime = new Date(cluster.createdAt)
  const executionTime = new Date(oldTime.getTime() + extraTime * 60000);
  return Math.round((executionTime.getTime() - new Date().getTime()) / 60000);
}

const getExtraInstance = (cluster) => {
  const total = getBatchModel(cluster.batchModelId).maxBatchSize;
  const currently = cluster.instances.length;
  return total - currently;
}

const resume = (processInstanceId) => {
  store.dispatch('ResumeProcessInstance', processInstanceId)
    .then(() => {
      init({
        color: "info",
        message: "resumed process instance"
      });
    })
    .catch((error) => {
      init({
        color: "danger",
        message: "something went wrong. " + error.message,
      });
    });
}

</script>

<style>
</style>
