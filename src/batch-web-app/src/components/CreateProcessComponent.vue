<template>
  <div>
    <h3>
      Create Process
    </h3>
    <p>
      NOTE: the batch controller cannot handle process update. Each process must have a different Id.
    </p>
    <div class="row">

      <va-input
        class="flex md6"
        v-model="data.resourceName"
        label="BPMN name"
        placeholder="MyProcess.bpmn"
      />
      <va-input
        class="flex md6"
        v-model="data.zeebeAddress"
        label="Zeebe API URI"
      />
      <va-input
        class="flex md12"
        v-model="data.resourceString"
        label="BPMN process XML content"
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
import { ref, defineEmits } from "vue";
import { useStore } from "vuex";
import { useToast } from 'vuestic-ui'

const emit = defineEmits(['finished'])
const { init } = useToast();
const store = useStore();

const data = ref({
  zeebeAddress: process.env.VUE_APP_ZEEBE_API,
  resourceString: "",
  resourceContent: "",
});

const submitForm = () => {
  data.value.resourceContent = data.value.resourceContent.replace(/(\r\n|\n|\r)/gm, "");
  store.dispatch('createProcess', data.value)
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
