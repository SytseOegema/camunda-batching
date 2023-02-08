<template>
  <div class="row">
    <div class="flex md8 offset-md2">
      <va-card>
        <div class="row my-3">
          <div class="va-title ml-3">
            Batch Cluster : {{ cluster.batchClusterId }}
          </div>
          <va-badge
            class="ml-auto mr-3"
            :text="cluster.state"
            color="info"
          />
        </div>
          <va-card-block class="flex-nowrap" horizontal>
            <va-card-block style="flex: 50;">
              <va-card-content>
                <div class="row">
                  <va-chip square class="mr-3" style="width: 50px; height: 50px">
                    {{ minutesRemaining }}
                  </va-chip>
                  <div style="margin: auto 0">
                    More minutes remaining until {{ activationText }}.
                  </div>
                </div>
                <div class="row mt-2" v-if="activationText === 'resumption'">
                  <va-chip square class="mr-3" style="width: 50px; height: 50px" color="warning">
                    {{ minInstanceDif }}
                  </va-chip>
                  <div style="margin: auto 0">
                    More instances required for batch execution.
                  </div>
                </div>
                <div class="row mt-2" v-if="activationText === 'execution'">
                  <va-chip square class="mr-3" style="width: 50px; height: 50px" color="info">
                    {{ maxInstanceDif }}
                  </va-chip>
                  <div style="margin: auto 0">
                    More instances required to trigger batch execution.
                  </div>
                </div>
              </va-card-content>
            </va-card-block>
            <va-divider vertical />
            <va-card-block style="flex: auto;">
              <va-card-content>
                <div class="prop">
                  <span class="name">
                    Batch Model
                  </span>
                  <span class="seperator">
                    :
                  </span>
                  <span class="value">
                    {{ model.name }}
                  </span>
                </div>
                <div class="prop">
                  <span class="name">
                    Function Name
                  </span>
                  <span class="seperator">
                    :
                  </span>
                  <span class="value">
                    {{ functionName }}
                  </span>
                </div>
                <div class="prop">
                  <span class="name">
                    Maximum instances
                  </span>
                  <span class="seperator">
                    :
                  </span>
                  <span class="value">
                    {{ model.maxBatchSize }}
                  </span>
                </div>
                <div class="prop">
                  <span class="name">
                    Minimal instances
                  </span>
                  <span class="seperator">
                    :
                  </span>
                  <span class="value">
                    {{ model.activationThresholdCases }}
                  </span>
                </div>
                <div class="prop">
                  <span class="name">
                    Maximal lifespan
                  </span>
                  <span class="seperator">
                    :
                  </span>
                  <span class="value">
                    {{ model.activationThresholdTime }} minutes
                  </span>
                </div>
                <div class="prop">
                  <span class="name">
                    Group by
                  </span>
                  <span class="seperator">
                    :
                  </span>
                  <span class="value">
                    `{{ model.groupBy[0] }}`
                  </span>
                </div>
              </va-card-content>
            </va-card-block>
        </va-card-block>
      </va-card>
    </div>

    <div class="flex md8 offset-md2">
      <BatchClusterDetailInstances :cluster="cluster" :model="model" />
    </div>

  </div>
</template>

<script setup>
import BatchClusterDetailInstances from "./BatchClusterDetailInstances";
import { computed } from "vue";
import { useRoute } from "vue-router";
import { useStore } from "vuex";

const route = useRoute();
const store = useStore();

const cluster = computed(() => {
  const clusters = store.getters["getBatchClusters"];
  const cluster = clusters.find((clus) => clus.batchClusterId === Number(route.params.id));
  return cluster;
})

const model = computed(() => {
  const models = store.getters["getBatchModels"];
  return models.find((model) => model.batchModelId === cluster.value.batchModelId);
})

const functionName = computed(() => {
  return model.value.batchExecutorURI.slice(57, -5);
})

const activationText = computed(() => {
  if (cluster.value.instances.length >= model.value.activationThresholdCases) {
    return "execution"
  }
  return "resumption"
})

const minutesRemaining = computed(() => {
  const extraTime = model.value.activationThresholdTime;
  const oldTime = new Date(cluster.value.createdAt);
  // + 60 to compensate for UTC date format.
  const executionTime = new Date(oldTime.getTime() + (60 + extraTime) * 60000);
  // alert(executionTime.getTime());
  // alert(new Date().getTime());
  // alert(executionTime.getTime() - new Date().getTime());
  return Math.round((executionTime.getTime() - new Date().getTime()) / 60000);
})

const minInstanceDif = computed(() => {
  return model.value.activationThresholdCases - cluster.value.instances.length;
})

const maxInstanceDif = computed(() => {
  return model.value.maxBatchSize - cluster.value.instances.length;
})

</script>

<style>
.prop {
  width: 24;
  font-size: 0.9em;
  color: #444444;
}

.name {
  width: 11em;
  display: inline-block;
}

.seperator {
  width: 2em;
  display: inline-block;
  text-align: center;
}


.value {
  width: 11em;
  display: inline-block;
}
</style>
