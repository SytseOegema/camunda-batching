<template>
  <div class="heightBox">
    <div class="va-h5">
      Create Batch Activity Connector
    </div>
    <div class="row">
       <va-date-input
        class="flex md6"
        v-model="data.validity"
        v-model:is-open="isOpen"
        label="Valid until"
        @update:modelValue="closeDateInput()"
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
        disabled
      />
      <va-input
        class="flex md6"
        v-model="data.activityId"
        label="Activity Id"
        disabled
      />
      <div class="flex md12">
        Condition:
      </div>
      <va-input
        class="flex md6"
        v-model="data.conditions[0].fieldName"
        label="Field Name"
      />
      <va-select
        class="flex md6"
        v-model="data.conditions[0].fieldType"
        :options="fieldTypeOptions"
        label="Field Type"
      />
      <va-select
        class="flex md6"
        v-model="data.conditions[0].compareOperator"
        :options="compareOperatorOptions"
        label="Compare Operator"
      />
      <va-input
        class="flex md6"
        v-model="data.conditions[0].compareValue"
        label="Compare Value"
      />

    </div>
    <va-button @click="submitForm()">
      Submit Form
    </va-button>
  </div>
</template>

<script setup>
import { ref, defineEmits, defineProps } from "vue";
import { useStore } from "vuex";
import { useToast } from 'vuestic-ui'

const { init } = useToast();
const isOpen = ref(false);

const closeDateInput = () => {
  isOpen.value = false;
}

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

const fieldTypeOptions = [
  "String",
  "Int",
  "Long",
  "Float",
  "Double",
  "Boolean",
]

const compareOperatorOptions = [
  "=",
  "<",
  ">",
]

const data = ref({
  active: activeOptions[0],
  validity: new Date(),
  batchModelId: props.batchModelId,
  activityId: props.activityId,
  conditions: [
    {
      conditionId: 0,
      connectorId:0,
      fieldName: "",
      fieldType: fieldTypeOptions[0],
      compareOperator: compareOperatorOptions[0],
      compareValue: "",
    }
  ],
});

function submitForm() {
  data.value.active = data.value.active.value;
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

.heightBox {
  height: 40vh;
}
</style>
