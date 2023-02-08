<template>
  <va-card>
    <div class="row">
      <p class="va-h4 ml-3 mt-3">
        {{ process.name }}
      </p>
      <va-button @click="togleModal" class="ml-auto">
        Create Process Instance
      </va-button>
    </div>
    <va-card-content>
      <div class="row">
        <div class="flex xs6">
          <h4>
            Activities
          </h4>
          <table class="va-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Type</th>
                <th>Batch Models</th>
                <th>Process Instances</th>
              </tr>
            </thead>
            <tbody>
              <ActivityComponent
                v-for="activity in process.activities"
                :key="activity.id"
                :activity="activity"
              />
            </tbody>
          </table>
        </div>
        <div class="flex xs6">
          <h4>
            Batch Connectors
          </h4>
          <BatchActivityTable :process="process" />
        </div>
        <div class="flex xs12">
          <h4>
            Process Instances
          </h4>
          <ProcessInstanceTable :process="process" />
        </div>
      </div>
    </va-card-content>
  </va-card>
  <va-modal v-model="showModal" blur>
    <template #content="{ ok }">
      <CreateProcessInstanceComponent :processId="process.id" @finished="ok"/>
    </template>
  </va-modal>

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
import ActivityComponent from "./ActivityComponent";
import BatchActivityTable from "./BatchActivityTable";
import ProcessInstanceTable from "./ProcessInstanceTable";
import CreateProcessInstanceComponent from "./CreateProcessInstanceComponent";
import { ref, defineProps } from 'vue'

defineProps({
  process: { type: Object, required: true },
})


const showModal = ref(false);
const togleModal = () => {
  showModal.value = true;
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
