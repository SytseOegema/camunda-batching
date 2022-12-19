<template>
  <div class="row mt-5">
    <h1 class="ml-4 va-h3">
      Batch Models
    </h1>
    <div class="ml-auto mr-4">
      <va-button class="mx-1" @click="togleModal()" v-if="!deleting">
        Create Batch Model
      </va-button>
      <va-button class="mx-1" @click="deleteModels()" color="danger" v-if="!deleting">
        Delete Batch Models
      </va-button>
      <va-button class="mx-1" @click="cancelDeletingModels()" size="large" color="danger" v-if="deleting">
        Cancel Deleting
      </va-button>
    </div>
  </div>
  <div class="row" v-if="batchModels.length === 0">
    No batch models available
  </div>
  <div class="row" v-else>
    <div
      class="flex md6"
      v-for="batchModel in batchModels"
      :key="batchModel.batchModelId"
    >
      <BatchModelComponent :batchModel="batchModel" :iconName="iconName" @click="(id) => deleteModel(id)" />
    </div>
  </div>
  <va-modal v-model="showModal" blur>
    <template #content="{ ok }">
      <CreateBatchModelComponent @finished="ok"/>
    </template>
  </va-modal>
</template>

<script setup>
import CreateBatchModelComponent from "./CreateBatchModelComponent.vue";
import BatchModelComponent from "./BatchModelComponent.vue";
import { ref, computed, onMounted } from 'vue'
import { useStore } from "vuex";
import { useToast } from 'vuestic-ui'

const { init } = useToast();

const store = useStore();

onMounted(() => {
  store.dispatch('getBatchModels');
})

const batchModels = computed(() => store.getters['getBatchModels']);

const iconName = computed(() => {
  if (deleting.value) {
    return "cancel"
  }
  return undefined
});

const deleting = ref(false);

const showModal = ref(false);

const togleModal = () => {
  showModal.value = !showModal.value;
}

const deleteModels = () => {
  deleting.value = true;
}

const cancelDeletingModels = () => {
  deleting.value = false;
}

const deleteModel = (batchModelId) => {
  store.dispatch('deleteBatchModel', batchModelId)
    .then(() => {
      init({
        color: "info",
        message: "deleted batch model"
      });
    });
}

</script>

<style>
</style>
