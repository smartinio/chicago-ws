import { SET_MY_HAND, SET_MY_TURN, SET_MY_PLAYER, SET_IM_DEALING } from './mutation_types'

const state = {
  isMyTurn: false,
  imDealing: false,
  hand: [] as any[],
  id: '',
  name: '',
  score: 0,
  hasTakenChicago: false,
  winner: false
}

type State = typeof state

const mutations = {
  [SET_MY_HAND] (state: State, value: any[]) {
    state.hand = value
  },
  [SET_MY_TURN] (state: State, value: boolean) {
    state.isMyTurn = value
  },
  [SET_IM_DEALING] (state: State, value: boolean) {
    state.imDealing = value
  },
  [SET_MY_PLAYER] (state: State, my: any) {
    state.id = my.id
    state.name = my.name
    state.score = my.score
    state.hasTakenChicago = my.hasTakenChicago
    state.winner = my.winner
  }
}

export const me = { state, mutations }
