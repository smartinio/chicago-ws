import { createStore } from 'vuex'
import { me } from './modules/me/me'
import { game } from './modules/game/game'
import { errors } from './modules/errors/errors'
import { socket } from './modules/socket/socket'

const modules = {
  me,
  game,
  errors,
  socket,
}

type Module = typeof modules

type State = {
  [K in keyof Module]: Module[K]['state'];
}

const store = createStore<State>({
  modules,
})

export default store

export type Store = typeof store
