import { createApp } from 'vue'
import App from './App.vue'
import { createVuesticEssential, VaButton } from 'vuestic-ui'
import 'vuestic-ui/styles/essential.css'
import 'vuestic-ui/styles/grid.css'
import 'vuestic-ui/styles/reset.css'
import 'vuestic-ui/styles/typography.css'
import { createProjectRouter } from '@/router';
import { useVuestic } from '@/vuestic';
import { store } from "@/store"

const app = createApp(App);

createProjectRouter(app);
useVuestic(app);

app.use(store);

app.use(createVuesticEssential({ components: { VaButton } })).mount('#app');
