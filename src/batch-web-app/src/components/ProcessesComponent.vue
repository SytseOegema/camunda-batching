<template>
  <div class="row">
    <h1 class="ml-4 va-h3">
      Processes
    </h1>
    <div class="ml-auto mr-4">
      <va-button class="mx-1" @click="togleModal()">
        Create Process
      </va-button>
    </div>
  </div>
  <div class="row" v-if="processes.length === 0">
    No processes available.
  </div>
  <div class="row" v-else>
    <div
      class="flex md12"
      v-for="process in processes"
      :key="process.id"
    >
      <ProcessComponent :process="process" />
    </div>
  </div>
  <va-modal v-model="showModal" blur>
    <template #content="{ ok }">
      <CreateProcessComponent @finished="ok"/>
    </template>
  </va-modal>
</template>

<script setup>
import CreateProcessComponent from "./CreateProcessComponent.vue";
import ProcessComponent from "./ProcessComponent.vue";
import { useStore } from "vuex";
import { ref, computed } from 'vue'

const store = useStore();

const processes = computed(() => store.getters['getProcesses']);

const showModal = ref(false);

const togleModal = () => {
  showModal.value = !showModal.value;
}

</script>

<style>
.va-card {
  margin: 10px 20px;
}

</style>
