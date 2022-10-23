import Vue from 'vue'
import App from './views/App'
import store from './store'
import helpers from './mixins/helpers'
import router from './router'
import VueChatScroll from 'vue-chat-scroll'

Vue.use(VueChatScroll)
Vue.config.productionTip = process.env.NODE_ENV === 'development'
Vue.mixin(helpers)

/* eslint-disable no-new */
new Vue({
  store,
  router,
  el: '#app',
  render: h => h(App)
}).$mount('#app')
