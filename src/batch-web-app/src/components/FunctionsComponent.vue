<template>
  <va-card>
    <va-card-content>
      <div class="va-h5">
        Create Batch Activity Connector
      </div>
      <div class="row">
        <va-input
          class="flex md6"
          v-model="data.validity"
          label="Valid for number of minutes"
        />
        <va-select
          class="flex md6"
          v-model="data.active"
          :options="activeOptions"
          label="Is active"
        />
        <va-input
          class="flex md6"
          v-model="data.batchModelId"
          label="Batch Model Id"
        />
        <va-input
          class="flex md6"
          v-model="data.activityId"
          label="Activity Id"
        />
      </div>
      <va-button @click="submitForm()">
        Submit Form
      </va-button>
    </va-card-content>
  </va-card>
</template>

<script setup>
import { ref, defineEmits, defineProps } from "vue";
import { useStore } from "vuex";
import { useToast } from 'vuestic-ui'

const { init } = useToast();

const store = useStore();
const emit = defineEmits(['finished'])
const props = defineProps({
  batchModelId: { type: Number, required: true},
  activityId: { type: String, required: true},
})

const activeOptions = [
  {
    text: "True",
    value: true,
  },
  {
    text: "False",
    value: false,
  }
]

const data = ref({
  active: activeOptions[0],
  validity: "3600",
  batchModelId: props.batchModelId,
  activityId: props.activityId,
  conditions: [],
});

function submitForm() {
  store.dispatch('createBatchActivityConnectors', data.value)
    .then(() => {
      init({
        color: "success",
        message: "created batch model"
      });
      emit("finished")
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
