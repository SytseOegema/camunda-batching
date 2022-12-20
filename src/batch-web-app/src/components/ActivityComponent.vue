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
      </div>
      <div class="row">
        <div class="flex md12" v-for="connector in connectors" :key="connector.id">
          <div class="row">
            <div class="va-title mr-4 mt-3">
              {{ getBatchModelName(connector.batchModelId) }}
            </div>

            <va-chip squared outline v-if="connector.active">
              active
            </va-chip>
            <va-chip squared outline v-else color="secondary">
              not active
            </va-chip>

            <va-button
              icon="delete"
              color="danger"
              round class="ml-auto"
              @click="deleteConnector(connector.connectorId)"
            />
            <div>
              Valid till:
              <va-badge
                class="mr-4"
                :text="connector.validity"
                color="info"
              />
              Conditions:
              {{ connector.conditions }}
            </div>
          </div>
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
import { useToast } from 'vuestic-ui'

const { init } = useToast();

const props = defineProps({
  activity: { type: Object, required: true },
})

const store = useStore();

const batchModels = computed(() => store.getters['getBatchModels']);
const connectors = computed(() => {
    const data = store.getters['getBatchActivityConnectors'];
    return data.filter((con) => {
      return con.activityId === props.activity.id;
    });
});

const getBatchModelName = (batchModelId) => {
  const batchModels = store.getters['getBatchModels'];
  return batchModels.find((model) => model.batchModelId === batchModelId).name;
}

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

</script>

<style>
.va-card {
  margin: 10px 20px;
}

.modalWidth {
  width: 40vw;
}
</style>
