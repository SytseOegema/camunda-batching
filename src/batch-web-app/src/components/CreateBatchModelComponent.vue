<template>
  <div>
    <h3>
      Batch Model
    </h3>
    <div class="row">
      <va-input
        class="flex md6"
        v-model="data.name"
        label="Name"
        placeholder="Name"
      />
      <va-input
        class="flex md6"
        v-model="data.maxBatchSize"
        label="maxBatchSize"
        placeholder="maxBatchSize"
      />
      <va-select
        class="flex md6"
        v-model="data.executeParallel"
        :options="executeParallelOptions"
        label="executeParallel"
        placeholder="executeParallel"
      />
      <va-input
        class="flex md6"
        v-model="data.activationThresholdCases"
        label="activationThresholdCases"
        placeholder="activationThresholdCases"
      />
      <va-input
        class="flex md6"
        v-model="data.activationThresholdTime"
        label="activationThresholdTime"
        placeholder="activationThresholdTime"
      />
      <va-input
        class="flex md6"
        v-model="data.batchExecutorURI"
        label="batchExecutorURI"
        placeholder="batchExecutorURI"
      />
    </div>
    <div class="flex md12">
      <div class="row">
        <va-button @click="toggleGroupBy()" class="ml-auto mr-auto">
          {{ groupByText }} Group By
        </va-button>
      </div>
      <span v-if="addGroupBy">
        Group By:
      </span>
    </div>
    <div class="row md12">
      <va-input
        v-if="addGroupBy"
        class="flex md6"
        v-model="data.groupBy[0]"
        label="Group By Variable"
      />
    </div>
    <va-button
      @click="submitForm()"
    >
      Submit
    </va-button>
  </div>
</template>

<script setup>
import { ref, defineEmits, computed } from "vue";
import { useStore } from "vuex";
import { useToast } from 'vuestic-ui'

const { init } = useToast();

const store = useStore();
const emit = defineEmits(['finished'])

const executeParallelOptions = [
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
  name: "",
  maxBatchSize: 5,
  executeParallel: executeParallelOptions[1],
  activationThresholdCases: 2,
  activationThresholdTime: 360,
  batchExecutorURI: "https://openwhisk_apigateway_1/api/v1/web/guest/batching/[name].json",
  groupBy: [],
});

const addGroupBy = ref(false);

const groupByText = computed(() => addGroupBy.value ? "remove" : "add");
const toggleGroupBy = () => {
  if (addGroupBy.value) {
    data.value.groupBy = [];
  } else {
    data.value.groupBy.push("");
  }
  addGroupBy.value = !addGroupBy.value;
}


function submitForm() {
  data.value.executeParallel = data.value.executeParallel.value;
  store.dispatch('createBatchModel', data.value)
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
.flex {
  margin: 10px 0px;
}

.va-card {
  padding: 10px 10px;
}

.va-input {
  padding: 5px 5px;
}

.va-dropdown {
  padding: 5px 5px;
}
</style>
