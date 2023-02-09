<template>
  <va-card>
    <div class="row my-3">
      <div class="va-title ml-3">
        Process instances in batch cluster
      </div>
      <va-badge
        class="ml-auto mr-3"
        :text="totalInstances + ' instances'"
        color="info"
      />
    </div>

      <table class="va-table">
        <thead>
          <tr>
            <th>Process Instance</th>
            <th>Process Name</th>
            <th>Group By Value</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="instance in processInstances"
            :key="instance.elementInstanceKey"
          >
            <td>{{ instance.processInstanceKey }}</td>
            <td>{{ instance.bpmnProcessId }}</td>
            <td>{{ getGroupByValue(instance) }}</td>
            <td>
              <va-button
                v-if="cluster.state === 'ready'"
                color="primary"
                round
                size="small"
                icon-right="forward"
                @click="resume(instance.processInstanceId)"
              >
                Resume
              </va-button>
              <va-button
                color="secondary"
                round
                class="ml-2"
                size="small"
                icon-right="visibility"
                @click="showBatchData(getGroupByValue(instance))"
              >
                Show batch data
              </va-button>
            </td>
          </tr>
        </tbody>
      </table>
  </va-card>

  <va-modal v-model="showModal" :message="batchData" blur />
</template>

<script setup>
import { defineProps, computed, ref } from 'vue';
import { useStore } from 'vuex';
import { useToast } from 'vuestic-ui'

const { init } = useToast();
const store = useStore();
const props = defineProps({
  cluster: { type: Object, required: true },
  model: { type: Object, required: true },
})

const showModal = ref(false);
const batchData = ref("")
const totalInstances = computed(() => props.cluster.instances.length);

const processInstances = computed(() => {
  const ids = props.cluster.instances.map((instance) => instance.processInstanceKey);
  // contains multiple records for a single instance.
  // one record per activity in the process flow
  let data = store.getters["getProcessInstances"];
  // filter processInstances for this process.
  data = data.filter((instance) => ids.includes(instance.processInstanceKey));

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
  data = Array.from(map.values());
  data = data.map((item) => {
    const idx = props.cluster.instances.findIndex(
      (instance) => instance.processInstanceKey === item.processInstanceKey
    );
    item.processInstanceId = props.cluster.instances[idx].processInstanceId;
    return item;
  });
  return data;
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

const getGroupByValue = (instance) => {
  const variables = JSON.parse(instance.variables);
  const result = variables[props.model.groupBy[0]];
  if (result === undefined) {
    return "";
  }
  return result;
}

const showBatchData = (groupByValue) => {
  showModal.value = true;
  const instances = processInstances.value.filter((inst) => {
    const variables = JSON.parse(inst.variables);
    const result = variables[props.model.groupBy[0]];
    if (result === undefined) {
      return false;
    }
    return result === groupByValue;
  })

  batchData.value = "[";
  instances.forEach((inst) => {
    batchData.value += inst.variables;
    batchData.value += ",\n";
  });
  batchData.value = batchData.value.slice(0, -2);
  batchData.value += "]";
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
