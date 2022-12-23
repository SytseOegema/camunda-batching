<template>
  <va-card>
    <div class="row">
    <va-card-title>
      {{ process.name }}
    </va-card-title>
    <va-button @click="togleModal" class="ml-auto">
      Create Process Instance
    </va-button>
    </div>
    <va-card-content>
      <div class="row">
        <div class="flex md12">
          <p class="va-h5">
            Activities
          </p>
          <ActivityComponent
            v-for="activity in process.activities"
            :key="activity.id"
            :activity="activity"
            :instances="processInstancesPerActivity(activity.id)"
          />
        </div>
        <div class="flex md12">
          <p class="va-h5">
            Finished Process Instances
          </p>
          <ProcessInstanceComponent :instance="otherInstanceFinalResult(instance.processInstanceKey)" v-for="instance in otherInstances" :key="instance.elementInstanceKey" />
        </div>
      </div>
    </va-card-content>
  </va-card>
  <va-modal v-model="showModal" blur>
    <template #content="{ ok }">
      <CreateProcessInstanceComponent :processId="process.id" @finished="ok"/>
    </template>
  </va-modal>
</template>

<script setup>
import ActivityComponent from "./ActivityComponent";
import ProcessInstanceComponent from "./ProcessInstanceComponent.vue";
import CreateProcessInstanceComponent from "./CreateProcessInstanceComponent";
import { useStore } from "vuex";
import { ref, computed, defineProps } from 'vue'

const props = defineProps({
  process: { type: Object, required: true },
})

const showModal = ref(false);
const store = useStore();

const togleModal = () => {
  showModal.value = true;
}

const otherInstanceFinalResult = (processInstanceKey) => {
  const data = store.getters["getProcessInstances"];
  data.sort((a, b) => sortFunction(a, b));

  return data.find((instance) => {
    return instance.processInstanceKey == processInstanceKey
    && instance.elementInstanceKey == processInstanceKey
  });
}

const processInstances = computed(() => {
  // contains multiple records for a single instance.
  // one record per activity in the process flow
  const data = store.getters["getProcessInstances"];
  const map = new Map();
  data.sort((a, b) => sortFunction(a, b));


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
});

const otherInstances = computed(() => {
  const activityIds = props.process.activities.map((activity) => activity.id);
  return processInstances.value.filter((instance) => !activityIds.includes(instance.elementId));
})


const processInstancesPerActivity = (acitivityId) => {
  return processInstances.value.filter((instance) => instance.elementId === acitivityId);
}

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
//
// const showModal = ref(false);
//
// const togleModal = () => {
//   showModal.value = !showModal.value;
// }

</script>

<style>
.va-card {
  margin: 10px 20px;
}

</style>
