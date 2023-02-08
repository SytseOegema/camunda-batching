<template>
  <va-badge
    class="mx-4"
    :text="instanceState(instance.intent)"
    :color="instanceColor(instance.intent)"
  />
</template>

<script setup>
import { defineProps } from 'vue'

defineProps({
  instance: { type: Object, required: true },
})

const instanceState = (instanceIntent) => {
  let result = "unknown";
  switch(instanceIntent) {
    case "readyElement":
      result = "Ready";
      break;
    case "resumeElement":
      result = "Active";
      break;
    case "completeElement":
      result = "Completed";
      break;
    case "terminateElement":
      result = "Terminated";
      break;
    case "Clustered":
      result = "Clustered";
      break;
    case "ActiveBatch":
      result = "ActiveBatch";
      break;
    default :
      result = "unknown";
      break;
  }
  return result;
}
const instanceColor = (instanceIntent) => {
  let result = "info";
  switch(instanceIntent) {
    case "readyElement":
      result = "info";
      break;
    case "resumeElement":
      result = "success";
      break;
    case "completeElement":
      result = "success";
      break;
    case "terminateElement":
      result = "danger";
      break;
    case "Clustered":
      result = "info";
      break;
    case "ActiveBatch":
      result = "success";
      break;
    default :
      result = "warning";
      break;
  }
  return result;
}

</script>

<style>
</style>
