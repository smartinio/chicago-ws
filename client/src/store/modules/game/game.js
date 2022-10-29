import { MIRROR_GAME_STATE, SET_STALE } from './mutation_types'
import { HANDLE_SNAPSHOT } from './action_types'
import { SET_MY_TURN, SET_MY_HAND, SET_MY_PLAYER, SET_IM_DEALING } from './../me/mutation_types'
import router from '@/router'
import { DISCONNECTED } from '../socket/action_types'

const state = {
  stale: false,
  host: {
    id: ''
  },
  dealer: {
    id: ''
  },
  invKey: '',
  players: [],
  events: [],
  started: false,
  hasWinners: false,
  round: {
    currentPlayer: '',
    phase: '',
    tricks: []
  }
}

const mutations = {
  [SET_STALE] (state, value) {
    state.stale = value
  },
  [MIRROR_GAME_STATE] (state, game) {
    state.stale = false
    state.host.id = game.host.id
    state.dealer = game.dealer
    state.invKey = game.invitationKey
    state.players = game.players
    state.events = game.events
    state.started = game.started
    state.hasWinners = game.hasWinners
    if (!game.currentRound) {
      return
    }
    state.round.currentPlayer = game.currentRound.currentPlayer
    state.round.phase = game.currentRound.phase
    state.round.tricks = game.currentRound.tricks
  }
}

const actions = {
  [DISCONNECTED] ({ commit }) {
    commit(SET_STALE, true)
  },
  [HANDLE_SNAPSHOT] ({ commit }, json) {
    const snapshot = JSON.parse(json)

    localStorage.setItem('invitationKey', snapshot.game.invitationKey)
    localStorage.setItem('playerId', snapshot.me.id)

    router.push(snapshot.game.invitationKey)
    commit(SET_MY_TURN, snapshot.myTurn)
    commit(SET_IM_DEALING, snapshot.imDealing)
    commit(SET_MY_HAND, snapshot.myHand)
    commit(SET_MY_PLAYER, snapshot.me)
    commit(MIRROR_GAME_STATE, snapshot.game)
  }
}

export default { state, mutations, actions }
