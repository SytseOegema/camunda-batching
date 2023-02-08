<template>
  <table class="va-table">
    <thead>
      <tr>
        <th>Status</th>
        <th>Process Instance</th>
        <th>Activity Name</th>
        <th>Batch Model Name</th>
        <th>Batch Cluster</th>
        <th>Actions</th>
      </tr>
    </thead>
    <tbody>
      <tr
        v-for="instance in processInstances"
        :key="instance.elementInstanceKey"
      >
        <td>
          <ProcessInstanceComponent :instance="instance" />
        </td>
        <td>{{ instance.processInstanceKey }}</td>
        <td>{{ instance.activityName }}</td>
        <td>
          {{ instance.batchModelName }}
        </td>
        <td>
          {{ instance.batchClusterId }}
        </td>
        <td>
          <va-button
            v-if="instance.intent === 'Clustered'"
            color="primary"
            round
            size="small"
            icon-right="forward"
            @click="resume(instance.processInstanceId)"
          >
            Resume
          </va-button>
          <va-button
            v-if="instance.intent === 'Clustered'"
            color="secondary"
            round
            class="ml-2"
            size="small"
            icon-right="visibility"
            @click="showCluster(instance.batchClusterId)"
          >
            Cluster Details
          </va-button>
        </td>
      </tr>
    </tbody>
  </table>

  <h4 class="mt-3">
    Finished Process Instances
  </h4>
  <div
    class="my-1"
    v-for="instance in otherInstances"
    :key="instance.elementInstanceKey"
  >
    <va-chip square>
      {{ instance.processInstanceKey }}
    </va-chip>
    <ProcessInstanceComponent :instance="instance" />
  </div>
</template>

<script setup>
import ProcessInstanceComponent from "./ProcessInstanceComponent";
import { defineProps, computed } from 'vue'
import { useStore } from "vuex";
import { useRouter } from "vue-router";
import { useToast } from 'vuestic-ui'

const { init } = useToast();
const store = useStore();
const router = useRouter();
const props = defineProps({
  process: { type: Object, required: true },
})

const processInstanceInProcess = computed(() => {
  // contains multiple records for a single instance.
  // one record per activity in the process flow
  let data = store.getters["getProcessInstances"];
  // filter processInstances for this process.
  data = data.filter((instance) => instance.bpmnProcessId === props.process.id);

  // Get the latest record of a process instance.
  data.sort((a, b) => sortFunction(a, b));

  const map = new Map();
  data.forEach((element) => {
    if(map.has(element.processInstanceKey)) {
      const item = map.get(element.processInstanceKey);
      if(item.elementInstanceKey < element.elementInstanceKey) {
        map.set(element.processInstanceKey, element);
      }
    } else {
      map.set(element.processInstanceKey, element);
    }
  });
  return Array.from(map.values());
})

const processInstances = computed(() => {
  // add activity name that the process instance is in
  // and filter out the instances not related to an activity.
  const data = []
  processInstanceInProcess.value.forEach((instance) => {
    const activity = props.process.activities.find((a) => a.id === instance.elementId);
    if (activity !== undefined) {
      instance.activityName = activity.name;
      data.push(instance);
    }
  });

  // Make connection with batch clusters to obtain information on clustered instances.
  const batchClusters = store.getters["getBatchClusters"];
  const batchModels = store.getters["getBatchModels"];
  batchClusters.forEach((cluster) => {
    cluster.instances.forEach((instance) => {
      const idx = data.findIndex(
        (procInstance) => procInstance.processInstanceKey === instance.processInstanceKey
      );
      if (idx !== -1) {
        data[idx].batchClusterId = cluster.batchClusterId;
        data[idx].processInstanceId = instance.processInstanceId;
        data[idx].intent = cluster.state === "ready" ? "Clustered" : "ActiveBatch";
        const model = batchModels.find((model) => model.batchModelId === cluster.batchModelId);
        if (model !== undefined) {
          data[idx].batchModelName = model.name;
        } else {
          data[idx].batchModelName = "";
        }
      }
    })
  });

  return data;
});

// const otherInstanceFinalResult = (processInstanceKey) => {
//   const data = store.getters["getProcessInstances"];
//   data.sort((a, b) => sortFunction(a, b));
//
//   return data.find((instance) => {
//     return instance.processInstanceKey == processInstanceKey
//     && instance.elementInstanceKey == processInstanceKey
//   });
// }


const otherInstances = computed(() => {
  const activityIds = props.process.activities.map((activity) => activity.id);
  return processInstanceInProcess.value.filter((instance) => !activityIds.includes(instance.elementId));
})

// function to sort instance records chronoligcal
const sortFunction = (a, b) => {
  if(a.elementInstanceKey < b.elementInstanceKey) {
    return -1;
  } else if (a.elementInstanceKey > b.elementInstanceKey) {
    return 1
  } else {
    return getIntentSortValue(a.intent) < getIntentSortValue(b.intent);
  }
}

// create an numerical order in first intent to last intent
const getIntentSortValue = (instanceIntent) => {
  let result = -1;
  switch(instanceIntent) {
    case "readyElement":
      result = 1;
      break;
    case "resumeElement":
      result = 2;
      break;
    case "completeElement":
      result = 3;
      break;
    case "terminateElement":
      result = 4;
      break;
    default :
      result = -1;
      break;
  }
  return result;
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

const showCluster = (clusterId) => {
  router.push({ name: 'BatchClusterDetails', params: { id: clusterId } });
}
</script>

<style>
.va-card {
  margin: 10px 20px;
}

.modalWidth {
  width: 40vw;
}

.va-badge__text {
  text-transform: none !important;
}
</style>
