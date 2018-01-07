import Vue from 'vue'
import Vuex from 'vuex'
import me from './modules/me/me'
import game from './modules/game/game'
import errors from './modules/errors/errors'
import socket from './modules/socket/socket'
import { ME, GAME, ERRORS, SOCKET } from './state_mappings'
Vue.use(Vuex)

const Store = new Vuex.Store({
  modules: {
    [ME]: me,
    [GAME]: game,
    [ERRORS]: errors,
    [SOCKET]: socket
  }
})

export default Store
