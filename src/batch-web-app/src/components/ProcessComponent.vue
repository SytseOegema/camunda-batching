<template>
  <va-card>
    <va-card-title>
      {{ process.name }}
    </va-card-title>
    <va-card-content>
      <div class="row">
        <div class="flex md6">
          <p class="va-h5">
            Activities
          </p>
          <ActivityComponent
            v-for="activity in process.activities"
            :key="activity.id"
            :activity="activity"
          />
        </div>
      </div>
    </va-card-content>
  </va-card>
</template>

<script setup>
import ActivityComponent from "./ActivityComponent";
// import { useStore } from "vuex";
import { defineProps } from 'vue'
// ref, computed, onMounted
defineProps({
  process: { type: Object, required: true },
})

// const store = useStore();
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
