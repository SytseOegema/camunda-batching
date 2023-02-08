<template>
  <tr>
    <td>{{ activity.name }}</td>
    <td>{{ activity.activityType }}</td>
    <td>
      {{ activity.connectors.length }}
      <va-button class="ml-5" @click="togleBatchModelModal()">
        +
      </va-button>
    </td>
    <td>{{ activity.activityType }}</td>
  </tr>


  <va-modal v-model="showBatchModelModal" blur size="large">
    <template #content="{ }">
      <div class="modalWidth">
        <div class="row">
          <div
            class="flex md12"
            v-for="batchModel in batchModels"
            :key="batchModel.batchModelId"
          >
            <BatchModelComponent :batchModel="batchModel" iconName="add" @click="(id) => createConnector(id)" />
          </div>
        </div>
      </div>
    </template>
  </va-modal>

  <va-modal v-model="showConnectorModal" blur>
    <template #content="{ ok }">
      <CreateBatchActivityConnectorComponent
        @finished="ok"
        :batchModelId="connectorBatchModelId"
        :activityId="activity.id"
      />
    </template>
  </va-modal>
</template>

<script setup>
import CreateBatchActivityConnectorComponent from "./CreateBatchActivityConnectorComponent.vue";
import BatchModelComponent from "./BatchModelComponent.vue";
import { useStore } from "vuex";
import { ref, defineProps, computed } from 'vue'

defineProps({
  activity: { type: Object, required: true },
})

const store = useStore();

const batchModels = computed(() => store.getters['getBatchModels']);
// const connectors = computed(() => {
//     const data = store.getters['getBatchActivityConnectors'];
//     return data.filter((con) => {
//       return con.activityId === props.activity.id;
//     });
// });

// const getBatchModelName = (batchModelId) => {
//   const batchModels = store.getters['getBatchModels'];
//   return batchModels.find((model) => model.batchModelId === batchModelId).name;
// }

const showBatchModelModal = ref(false);
const showConnectorModal = ref(false);
const connectorBatchModelId = ref(0);

const togleBatchModelModal = () => {
  showBatchModelModal.value = !showBatchModelModal.value;
}

const togleConnectorModal = () => {
  showConnectorModal.value = !showConnectorModal.value;
}

const createConnector = (batchModelId) => {
  connectorBatchModelId.value= batchModelId;
  showBatchModelModal.value = false;
  togleConnectorModal();
}

</script>

<style>
.va-card {
  margin: 10px 20px;
}

.modalWidth {
  width: 40vw;
}
</style>
