import { MIRROR_GAME_STATE, SET_STALE, SET_CURRENTLY_IN_GAME } from './mutation_types'
import { HANDLE_SNAPSHOT, HANDLE_CURRENTLY_IN_GAME } from './action_types'
import { SET_MY_TURN, SET_MY_HAND, SET_MY_PLAYER, SET_IM_DEALING } from './../me/mutation_types'
import router from '@/router'
import { DISCONNECTED } from '../socket/action_types'

const state = {
  currentlyInGame: undefined as boolean | undefined,
  stale: false,
  rules: {
    chicagoBestHand: false,
    oneOpen: 'ALL',
    numTrades: 2,
  },
  oneOpen: {
    player: {
      id: '',
    },
    isOpen: false,
    card: {
      suit: '',
      value: '',
      shortValue: '',
    }
  },
  host: {
    id: ''
  },
  dealer: {
    id: ''
  },
  invKey: '',
  players: [] as any[],
  events: [] as any[],
  started: false,
  hasWinners: false,
  round: {
    currentPlayer: '',
    chicagoTaker: '',
    phase: '',
    tricks: [] as any[],
    isFinalTrade: false,
  }
}

type State = typeof state

type Game = any

const mutations = {
  [SET_CURRENTLY_IN_GAME] (state: State, value: boolean) {
    state.currentlyInGame = value
  },
  [SET_STALE] (state: State, value: boolean) {
    state.stale = value
  },
  [MIRROR_GAME_STATE] (state: State, game: Game) {
    state.currentlyInGame = true
    state.stale = false
    state.host.id = game.host.id
    state.dealer = game.dealer
    state.invKey = game.invitationKey
    state.players = game.players
    state.events = game.events
    state.started = game.started
    state.hasWinners = game.hasWinners

    state.rules.chicagoBestHand = game.rules.chicagoBestHand
    state.rules.oneOpen = game.rules.oneOpen
    state.rules.numTrades = game.rules.numTrades

    if (!game.currentRound) {
      return
    }
    state.round.chicagoTaker = game.currentRound.chicagoTaker
    state.round.currentPlayer = game.currentRound.currentPlayer
    state.round.phase = game.currentRound.phase
    state.round.tricks = game.currentRound.tricks
    state.round.isFinalTrade = game.currentRound.isFinalTrade

    state.oneOpen.player = game.oneOpen.player
    state.oneOpen.card = game.oneOpen.card
    state.oneOpen.isOpen = game.oneOpen.isOpen
  }
}

const actions = {
  [HANDLE_CURRENTLY_IN_GAME] ({ commit }: any, json: any) {
    commit(SET_CURRENTLY_IN_GAME, JSON.parse(json))
  },
  [DISCONNECTED] ({ commit }: any) {
    commit(SET_STALE, true)
  },
  [HANDLE_SNAPSHOT] ({ commit }: any, json: any) {
    const snapshot = JSON.parse(json)

    requestAnimationFrame(() => {
      localStorage.setItem('invitationKey', snapshot.game.invitationKey)
      localStorage.setItem('playerId', snapshot.me.id)
    })

    router.push(snapshot.game.invitationKey)
    commit(SET_MY_TURN, snapshot.myTurn)
    commit(SET_IM_DEALING, snapshot.imDealing)
    commit(SET_MY_HAND, snapshot.myHand)
    commit(SET_MY_PLAYER, snapshot.me)
    commit(MIRROR_GAME_STATE, snapshot.game)
  }
}

export default { state, mutations, actions }
