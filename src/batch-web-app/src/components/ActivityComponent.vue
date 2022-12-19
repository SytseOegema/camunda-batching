<template>
  <va-card>
    <va-card-content>
      <div class="row">
        <div class="va-title">
          {{ activity.name }}
        </div>
        <div class="ml-auto mr-1 mt-1">
          <va-chip squared outline>
            {{ activity.activityType }}
          </va-chip>
        </div>
      </div>
      <div class="row mt-3">
        <div class="va-h6">
          Batch Models
        </div>
        <div class="ml-auto mr-1 mt-1">
          <va-button color="secondary" @click="togleBatchModelModal()">
            Add Batch Model
          </va-button>
        </div>
        <div>
          {{ connectors }}
        </div>
      </div>
    </va-card-content>
  </va-card>


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
    <template #content="{ }">
      <div class="row">
        Here goes the connector form
      </div>
    </template>
  </va-modal>

</template>

<script setup>
import BatchModelComponent from "./BatchModelComponent.vue";
import { useStore } from "vuex";
import { ref, defineProps, computed } from 'vue'

defineProps({
  activity: { type: Object, required: true },
})

const store = useStore();

const batchModels = computed(() => store.getters['getBatchModels']);
const connectors = computed(() => store.getters['getBatchActivityConnectors']);

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
