import { MIRROR_GAME_STATE } from './mutation_types'
import { HANDLE_SNAPSHOT } from './action_types'
import { SET_MY_TURN, SET_MY_HAND, SET_MY_PLAYER } from './../me/mutation_types'
import router from '@/router'

const state = {
  host: {
    id: ''
  },
  invKey: '',
  events: [],
  players: [],
  started: false,
  round: {
    currentPlayer: '',
    phase: '',
    tricks: []
  }
}

const mutations = {
  [MIRROR_GAME_STATE] (state, game) {
    state.host.id = game.host.id
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
  [HANDLE_SNAPSHOT] ({ commit }, json) {
    const snapshot = JSON.parse(json)
    router.push(snapshot.game.invitationKey)
    commit(SET_MY_TURN, snapshot.myTurn)
    commit(SET_MY_HAND, snapshot.myHand)
    commit(SET_MY_PLAYER, snapshot.me)
    console.log(snapshot)
    commit(MIRROR_GAME_STATE, snapshot.game)
  }
}

export default { state, mutations, actions }
