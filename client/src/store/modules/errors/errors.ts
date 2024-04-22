import { SET_NICKNAME_ERROR, SET_KEY_ERROR } from './mutation_types'
import { HANDLE_GAME_ERROR, HANDLE_JSON_ERROR, HANDLE_FATAL_ERROR } from './action_types'

const state = {
  nickname: '',
  invKey: ''
}

type State = typeof state

const mutations = {
  [SET_NICKNAME_ERROR] (state: State, error: string) {
    state.nickname = error
  },
  [SET_KEY_ERROR] (state: State, error: string) {
    state.invKey = error
  }
}

const actions = {
  [HANDLE_GAME_ERROR] ({state}: {state: State}, error: string) {
    window.alert(error)
  },
  [HANDLE_JSON_ERROR] () {
    window.alert('Something went wrong! Please try again')
  },
  [HANDLE_FATAL_ERROR] (_: {state: State}, error: string) {
    window.alert(error)
    localStorage.removeItem('invitationKey')
    localStorage.removeItem('playerId')
    window.location.href = '/'
  }
}

export const errors = { state, mutations, actions }
