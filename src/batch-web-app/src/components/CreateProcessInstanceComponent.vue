<template>
  <div>
    <h3>
      Create Process
    </h3>
    <div class="row">
      <va-input
        class="flex md12"
        v-model="data.zeebeAddress"
        label="Zeebe API URI"
      />
      <va-input
        class="flex md12"
        v-model="data.variables"
        label="JSON object with variables"
        type="textarea"
        :min-rows="12"
        :max-rows="12"
      />
    </div>
    <va-button
      @click="submitForm()"
    >
      Create
    </va-button>
  </div>

</template>

<script setup>
import { ref, defineEmits, defineProps } from "vue";
import { useStore } from "vuex";
import { useToast } from 'vuestic-ui'

const props = defineProps({
  processId: { require: true, type: String },
});

const emit = defineEmits(['finished'])
const { init } = useToast();
const store = useStore();

const data = ref({
  zeebeAddress: process.env.VUE_APP_ZEEBE_API,
  variables: "{}",
});

const submitForm = () => {
  store.dispatch('createProcessInstance', { data: data.value, processId: props.processId })
    .then(() => {
      init({
        color: "success",
        message: "created process"
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
</style>
