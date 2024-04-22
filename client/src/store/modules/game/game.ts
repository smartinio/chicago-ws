import { MIRROR_GAME_STATE, SET_STALE, SET_CURRENTLY_IN_GAME } from './mutation_types'
import { HANDLE_SNAPSHOT, HANDLE_CURRENTLY_IN_GAME } from './action_types'
import { SET_MY_TURN, SET_MY_HAND, SET_MY_PLAYER, SET_IM_DEALING } from './../me/mutation_types'
import router from '@/router'
import { DISCONNECTED } from '../socket/action_types'
import { GameEvent, GamePhase, Player, Suit, Trick, Value } from '@/server-types'

const state = {
  currentlyInGame: undefined as boolean | undefined,
  stale: false,
  rules: {
    chicagoBefore15: false,
    chicagoBestHand: false,
    roundWinScore: 5,
    tradeBanScore: 45,
    winWithTwoScore: 10,
    numTrades: 2,
    oneOpen: 'ALL',
  },
  oneOpen: {
    player: {
      id: '',
      name: '',
    },
    isOpen: false,
    card: {
      suit: '' as Suit,
      value: '' as Value,
      shortValue: '',
    }
  },
  resetOthersScore: {
    player: {
      id: '',
      name: '',
    },
    isPending: false,
    points: 0,
  },
  host: {
    id: ''
  },
  dealer: {
    id: ''
  },
  invKey: '',
  players: [] as Player[],
  events: [] as GameEvent[],
  started: false,
  hasWinners: false,
  round: {
    currentPlayer: null as Player | null,
    chicagoTaker: null as Player | null,
    winner: null as Player | null,
    phase: 'BEFORE' as GamePhase,
    tricks: [] as Trick[],
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
    state.rules.roundWinScore = game.rules.roundWinScore
    state.rules.tradeBanScore = game.rules.tradeBanScore
    state.rules.winWithTwoScore = game.rules.winWithTwoScore
    state.rules.chicagoBefore15 = game.rules.chicagoBefore15

    if (!game.currentRound) {
      return
    }

    state.round.chicagoTaker = game.currentRound.chicagoTaker
    state.round.winner = game.currentRound.winner
    state.round.currentPlayer = game.currentRound.currentPlayer
    state.round.phase = game.currentRound.phase
    state.round.tricks = game.currentRound.tricks
    state.round.isFinalTrade = game.currentRound.isFinalTrade

    state.oneOpen.player = game.oneOpen.player
    state.oneOpen.card = game.oneOpen.card
    state.oneOpen.isOpen = game.oneOpen.isOpen

    state.resetOthersScore.player = game.resetOthersScore.player
    state.resetOthersScore.isPending = game.resetOthersScore.isPending
    state.resetOthersScore.points = game.resetOthersScore.points
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


export const game = {
  state,
  mutations,
  actions,
};
