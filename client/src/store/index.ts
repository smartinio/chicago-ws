import { createStore } from 'vuex'
import me from './modules/me/me'
import game from './modules/game/game'
import errors from './modules/errors/errors'
import socket from './modules/socket/socket'
import { ME, GAME, ERRORS, SOCKET } from './state_mappings'

const store = createStore({
  modules: {
    [ME]: me,
    [GAME]: game,
    [ERRORS]: errors,
    [SOCKET]: socket
  }
})

export default store
