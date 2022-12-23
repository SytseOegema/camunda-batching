<template>
  <div class="app-layout">
    <va-navbar>
      <template v-slot:left>
        <div class="left">
          <span @click="returnBaseClick()" class="project-title">
            <va-navbar-item
              class="project-title"
              @click="goToRoute('Dashboard')"
            >
              Batching
            </va-navbar-item>
          </span>
        </div>
      </template>
      <template #right>
        <va-navbar-item
          :class="routePath === '/processes' ? 'nav-button active-route' :'nav-button'"
          @click="goToRoute('Processes')"
        >
          Processes
        </va-navbar-item>
        <va-navbar-item
          :class="routePath === '/functions' ? 'nav-button active-route' :'nav-button'"
          @click="goToRoute('Functions')"
        >
          Functions
        </va-navbar-item>
        <va-navbar-item
          :class="routePath === '/batch-models' ? 'nav-button active-route' :'nav-button'"
          @click="goToRoute('BatchModels')"
        >
          Batch Models
        </va-navbar-item>
        <va-navbar-item
          :class="routePath === '/batch-clusters' ? 'nav-button active-route' :'nav-button'"
          @click="goToRoute('BatchClusters')"
        >
          Batch Clusters
        </va-navbar-item>
      </template>
    </va-navbar>
    <div class="app-layout__content">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from "vue"
import { useRoute, useRouter } from "vue-router";
import { useStore } from "vuex";

const store = useStore();
const route = useRoute ();
const router = useRouter ();

const routePath = computed(() => route.fullPath);

const goToRoute = (name) => {
  router.push({name});
}

onMounted(() => {
  store.dispatch('getProcesses');
  store.dispatch('getProcessInstances');
  store.dispatch('getBatchModels');
  store.dispatch('getBatchActivityConnectors');
  store.dispatch('getBatchClusters');
})

</script>

<style>
.va-navbar {
  box-shadow: var(--va-box-shadow);
  z-index: 2;
  &__center {
    @media screen and (max-width: 1250px) {
      .app-navbar__text {
        display: none;
      }
    }
  }
  @media screen and (max-width: 1250px) {
    .left {
      width: 100%;
    }
    .app-navbar__actions {
      width: 100%;
      display: flex;
      justify-content: space-between;
    }
  }
}

.va-navbar__right {
  height: 100%;
}

.nav-button {
  padding: 0px 10px;
  font-size: 1.1em;
  font-family: Gayathri, sans-serif;
}

.nav-button:hover {
  background-color: var(--va-primary);
  color: white;
  font-weight: 600;
  cursor: pointer;
}

.active-route {
  background-color: var(--va-primary);
  color: white;
  font-weight: 600;
  cursor: pointer;
}

.project-title {
  margin-right: 0;
  font-size: 1.5em;
  font-family: Gayathri, sans-serif;
  color: var(--va-primary);
}


.project-title:hover {
  cursor: pointer;
}


.left {
  display: flex;
  align-items: center;
  & > * {
    margin-right: 1.5rem;
  }
  & > *:last-child {
    margin-right: 0;
  }
}

.app-layout__content {
  padding: 4px 10px;
}
</style>
