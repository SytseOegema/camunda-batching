import {
  createRouter,
  createWebHashHistory,
} from "vue-router";
import ProcessesComponent from "@/components/ProcessesComponent.vue";
import DashboardComponent from "@/components/DashboardComponent.vue";
import FunctionsComponent from "@/components/FunctionsComponent.vue";
import BatchClustersComponent from "@/components/BatchClustersComponent.vue";
import BatchClusterDetailComponent from "@/components/BatchClusterDetailComponent.vue";
import BatchModelsComponent from "@/components/BatchModelsComponent.vue";

const routes = [
  {
    path: '/',
    component: DashboardComponent,
    name: 'Dashboard'
  },
  {
    path: '/processes',
    component: ProcessesComponent,
    name: 'Processes'
  },
  {
    path: '/functions',
    component: FunctionsComponent,
    name: 'Functions'
  },
  {
    path: '/batch-clusters',
    component: BatchClustersComponent,
    name: 'BatchClusters'
  },
  {
    path: '/batch-clusters/:id',
    component: BatchClusterDetailComponent,
    name: 'BatchClusterDetails'
  },
  {
    path: '/batch-models',
    component: BatchModelsComponent,
    name: 'BatchModels'
  },
]

export const createProjectRouter = (app) => {
  const router = createRouter({
    history: createWebHashHistory(),
    routes,
  })

  app.use(router);
}
