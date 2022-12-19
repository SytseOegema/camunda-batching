<template>
  <va-card
    :class="showHoverEffect ? 'container point-on-hover' : 'container'"
    @click="cardClicked()"
  >
    <div
      v-if="showHoverEffect"
      class="row custom-icon"
      @mouseover="mouseOverIcon()"
      @mouseleave="mouseLeaveIcon()"
    >
      <va-icon
        class="mx-auto my-auto"
        v-if="showIcon"
        :name="iconName"
        size="100px"
      />
    </div>
    <div class="content">
      <div class="row">
        <div class="va-h5">
          {{ batchModel.name }}
        </div>
        <div class="ml-auto mr-1 mt-2">
          <va-chip outline v-if="batchModel.executeParallel">
            Parallel
          </va-chip>
          <va-chip outline color="secondary">
            Sequential
          </va-chip>
        </div>
      </div>

      <va-card-content>
        <div class="row">
          <p class="va-title">
            {{ batchModel.batchExecutorURI }}
          </p>
        </div>

        <div class="row">
          <div>
            <p class="va-h6 ">
              Activation
            </p>
            Requires
            <va-badge
              class="mr-4"
              :text="batchModel.activationThresholdCases + ' instances'"
              color="info"
            />
            every
            <va-badge
              class="mr-4"
              :text="batchModel.activationThresholdTime + ' minutes'"
              color="info"
            />
            <br/>
            Activates on
            <va-badge
              class="mr-4"
              :text="batchModel.maxBatchSize + ' instances'"
              color="secondary"
            />
          </div>
        </div>
        <div class="mt-5">
          <p class="va-h6 ">
            Group By
          </p>
          <br/>
          <p>
            {{ batchModel.groupBy }}
          </p>
        </div>
      </va-card-content>
    </div>
  </va-card>
</template>

<script setup>
import { defineProps, defineEmits, computed, ref } from "vue";

const mouseOnIcon = ref(false);

const emit = defineEmits(['click'])

const props = defineProps({
  batchModel: { type: Object, required: true },
  iconName: { type: String, required: false }
})


const showHoverEffect = computed(() => {
  if (props.iconName === null || props.iconName === undefined) {
    return false;
  }
  return true;
});


const showIcon = computed(() => {
  if (showHoverEffect.value) {
    return mouseOnIcon.value;
  }
  return false;
});

const mouseOverIcon = () => {
  mouseOnIcon.value = true;
}

const mouseLeaveIcon = () => {
  mouseOnIcon.value = false;
}

const cardClicked = () => {
  if (showHoverEffect.value) {
    emit("click", props.batchModel.batchModelId);
  }
}

</script>

<style>
.container {
  position: relative;
}

.content {
  display: block;
}

.custom-icon {
  position: absolute;
  top: 0;
  padding-top: 40;
  left: 0;
  z-index: 5;
  width: 100%;
  height: 100%;
}

.custom-icon:hover {
  background: rgba(1, 1, 1, 0.1);
}

.point-on-hover {
  cursor: pointer;
}
</style>
