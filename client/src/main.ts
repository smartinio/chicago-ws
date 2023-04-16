import { createApp } from 'vue'
import App from './views/App.vue'
import helpers from './mixins/helpers'
import router from './router'
import store from './store'

createApp(App)
  .use(router)
  .use(store)
  .mixin(helpers)
  .mount('#app')
