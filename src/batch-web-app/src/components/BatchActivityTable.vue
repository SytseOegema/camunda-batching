<template>
  <table class="va-table">
    <thead>
      <tr>
        <th></th>
        <th>Activity Name</th>
        <th>Batch Model Name</th>
        <th>Valid till</th>
        <th>Condition</th>
      </tr>
    </thead>
    <tbody>
      <tr
        v-for="connector in connectors"
        :key="connector.connectorId"
        @mouseover="mouseOverRow(connector.connectorId)"
        @mouseleave="mouseLeaveRow()"
      >
        <td>
          <va-button
            v-if="hoverRow === connector.connectorId"
            color="danger"
            round
            small
            icon="delete"
            @click="deleteConnector(connector.connectorId)"
          >
          </va-button>
        </td>
        <td>{{ connector.activityId }}</td>
        <td>{{ connector.batchModelName }}</td>
        <td>{{ connector.validity }}</td>
        <td v-if="connector.conditions.length > 0">
          <va-badge
            class="ml-auto mr-3"
            :text="connector.conditions[0].fieldName"
            color="secondary"
          />
          <va-badge
            class="ml-auto mr-3"
            :text="operator(connector.conditions[0].compareOperator)"
            color="primary"
          />
          <va-badge
            class="ml-auto mr-3"
            :text="connector.conditions[0].compareValue"
            color="secondary"
          />
        </td>
      </tr>
    </tbody>
  </table>
</template>

<script setup>
import { defineProps, computed, ref } from 'vue'
import { useStore } from "vuex";
import { useToast } from 'vuestic-ui'

const hoverRow = ref(0);
const { init } = useToast();
const props = defineProps({
  process: { type: Object, required: true },
})
const store = useStore();

const batchModels = computed(() => store.getters['getBatchModels']);

const getBatchModelName = (id) => {
  const model = batchModels.value.find((model) => model.batchModelId === id);
  if (model !== undefined) {
    return model.name;
  }
  return "";
}

const connectors = computed(() => {
  const cons = [];
  props.process.activities.forEach(activity => {
    activity.connectors.forEach((connector) => {
      connector.batchModelName = getBatchModelName(connector.batchModelId);
      cons.push(connector);
    });
  });
  return cons;
})

const operator = (text) => {
  let value = "="
  switch (text) {
    case "EQUALS":
      value = "=";
      break;
    case "LARGER_THAN":
      value = ">";
      break;
    case "SMALLER_THAN":
      value = ">";
      break;
    default:
      value = "=";
      break;
  }
  return value;
}

const deleteConnector = (connectorId) => {
  store.dispatch('deleteBatchActivityConnectors', connectorId)
    .then(() => {
      init({
        color: "info",
        message: "deleted batch model"
      });
    })
    .catch((error) => {
      init({
        color: "danger",
        message: "something went wrong. " + error.message,
      });
    });
}

const mouseOverRow = (connectorId) => {
  hoverRow.value = connectorId;
}

const mouseLeaveRow = () => {
  hoverRow.value = 0;
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
