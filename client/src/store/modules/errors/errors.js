import { SET_NICKNAME_ERROR, SET_KEY_ERROR } from './mutation_types'
import { HANDLE_GAME_ERROR, HANDLE_JSON_ERROR, HANDLE_FATAL_ERROR } from './action_types'

const state = {
  nickname: '',
  invKey: ''
}

const mutations = {
  [SET_NICKNAME_ERROR] (state, error) {
    state.nickname = error
  },
  [SET_KEY_ERROR] (state, error) {
    state.invKey = error
  }
}

const actions = {
  [HANDLE_GAME_ERROR] ({state}, error) {
    window.alert(error)
  },
  [HANDLE_JSON_ERROR] ({state}, error) {
    window.alert('Something went wrong! Please try again')
  },
  [HANDLE_FATAL_ERROR] ({state}, error) {
    window.alert(error)
    localStorage.removeItem('invitationKey')
    localStorage.removeItem('playerId')
    window.location.href = '/'
  }
}

export default { state, mutations, actions }
